# PrintScript Linter

El módulo `linter` realiza análisis estático de estilo sobre un AST de PrintScript
ya parseado y ya validado. Responde preguntas como:

- ¿Este identificador respeta la convención de nombres configurada (camelCase o
  snake_case)?
- ¿`println(...)` recibe un identificador o literal directo, en vez de una
  expresión compuesta, cuando esa restricción está activada?

No decide si el código es sintáctica o semánticamente correcto — eso ya pasó en
el parser y en el validator. El linter solo expresa *opiniones de estilo*,
configurables por proyecto, sobre código que ya se sabe que es válido.

## Dónde se ubica en el pipeline

```text
Sequence<Char>
      |
      v
   Lexer  --Token-->  Parser  --Node-->  Validator  --Node-->  Linter  -->  Sequence<Diagnostic>
                                                                  ^
                                                                  |
                                                        Collection<Rule> (config)
```

A diferencia del lexer y el parser, el linter no transforma su entrada — solo
*observa* el AST y reporta hallazgos. Además recibe una segunda entrada que las
etapas anteriores no necesitan: un `Collection<Rule>`, la configuración de lint
del proyecto (por ejemplo, leída desde un archivo JSON por un futuro módulo CLI).

## API pública

```kotlin
interface Linter {
    fun lint(version: String, nodes: Sequence<Node>, rules: Collection<Rule>): Sequence<Diagnostic>
}
```

Dos detalles distinguen este contrato del de `Lexer`/`Parser`:

- **Sin `Outcome`.** `lint` devuelve `Sequence<Diagnostic>` directamente, no
  `Sequence<Outcome<Node, Diagnostic>>`. Un hallazgo de lint no es "en vez de" un
  nodo válido — el nodo ya es válido; el hallazgo es una observación adicional y
  opcional sobre él. No hay nada que distinguir de un "Ok".
- **Nunca corta antes de tiempo.** Un mismo nodo puede producir cero, uno o
  varios diagnósticos (uno por cada regla activa que objete algo), y un
  diagnóstico en un nodo no impide seguir descendiendo a sus hijos. Compará esto
  con el parser, que se detiene en el primer `ParseError` — lintear consiste,
  fundamentalmente, en juntar *todos* los hallazgos en una sola pasada, no en
  fallar rápido.

## El modelo `Rule` (en `common`)

```kotlin
data class Rule(val signature: String, val value: RuleValue)
interface RuleType { val signature: String; val name: String }
interface RuleValue { fun format(): String; fun type(): String }

data class StringRuleValue(val value: String) : RuleValue {  }
data class BooleanRuleValue(val value: Boolean) : RuleValue {  }
data class IntegerRuleValue(val value: Int) : RuleValue {  }
```

Un `Rule` es, a propósito, un dato de configuración sin tipar: una `signature`
(una clave de tipo string, como `"identifier_format"`) y un `value` opaco. Es la
forma natural en la que se deserializa un archivo de config, antes de que nada
adentro del linter sepa qué significa una regla en particular. `RuleType` es la
identidad propia del linter, más rica, para una regla a nivel de código (se usa
para etiquetar diagnósticos, así el lector sabe qué regla los produjo);
`RuleType.signature` es lo que conecta a los dos — es la clave por la que se
busca un `Rule` proveniente de la configuración.

## Reutilizando `Visitor` de `common`

El linter no inventa su propio recorrido del AST. Cada regla activa se convierte
en un `Visitor` (la misma interfaz `Visitor` de `common.model.visitor`, ya usada
conceptualmente — aunque todavía no implementada — por el `Node` del parser). Una
regla que no aplica a un nodo dado simplemente devuelve `Outcome.Ok` sin
diagnóstico; una que sí aplica y encuentra un problema, devuelve
`Outcome.Error(lint)`.

```kotlin
internal class IdentifierFormatVisitor(
    private val formatName: String,
    private val regex: Regex,
) : Visitor {
    override fun visit(node: Node.Leaf, table: VisitorTable): Outcome<Value, Diagnostic> {
        if (node.type != IdentifierNode) return Outcome.Ok(node.value)
        val identifier = node.value.format()
        return if (regex.matches(identifier)) {
            Outcome.Ok(node.value)
        } else {
            Outcome.Error(Lint("El identificador '$identifier' no respeta el formato $formatName", node.span, IdentifierFormatRule, Warning))
        }
    }
    override fun visit(node: Node.Composite, table: VisitorTable) = Outcome.Ok(NoneValue)
}
```

Cada regla solo mira las formas de nodo que le importan (un `Leaf` de tipo
`IdentifierNode`, o un `Composite` de tipo `PrintlnStatementNode`) e ignora todo
lo demás devolviendo `Outcome.Ok`. Esto mantiene cada regla chica e independiente
— el mismo patrón de "muchas piezas chicas, una tabla que las orquesta" ya usado
para `GrammarTable` en el parser.

## De la configuración a los visitors: `VisitorFactory` y `VisitorTableBuilder`

Un `Rule` de la configuración es dato crudo (`"identifier_format" -> "camel
case"`). Algo tiene que convertir eso en un `IdentifierFormatVisitor` ya
configurado, con su `Regex` ya armada. Eso es `VisitorFactory`:

```kotlin
internal interface VisitorFactory {
    val ruleType: RuleType
    fun create(rule: Rule): Visitor
}

internal class IdentifierFormatVisitorFactory : VisitorFactory {
    override val ruleType: RuleType = IdentifierFormatRule
    private val formatPatterns = mapOf(
        "snake case" to Regex("^[a-z]+(?:_[a-z0-9]+)*$"),
        "camel case" to Regex("^[a-z][a-zA-Z0-9]*$"),
    )
    override fun create(rule: Rule): Visitor {
        val format = (rule.value as StringRuleValue).value.lowercase()
        return IdentifierFormatVisitor(format, formatPatterns[format] ?: Regex(".*"))
    }
}
```

`VisitorTableBuilder.build(rules: Collection<Rule>)` conecta cada `Rule` de la
config con su fábrica (emparejadas por `signature`), y produce una
`VisitorTable` (la misma interfaz `VisitorTable` de `common`, solo que ahora
contiene visitors de lint en vez de visitors del intérprete) — o un
`Outcome.Error(ConfigurationError)` si el valor de una regla tiene el tipo
incorrecto (por ejemplo, un string donde se esperaba un booleano), capturado con
un `ClassCastException` en el único punto donde ocurre el cast, en vez de dejar
que se propague como un crash sin manejar.

```kotlin
internal object PrintScriptV10 : VisitorTableBuilder {
    override val factories: Map<String, VisitorFactory> = mapOf(
        IdentifierFormatRule.signature to IdentifierFormatVisitorFactory(),
        MandatoryIdentifierOrLiteralInPrintlnRule.signature to MandatoryIdentifierOrLiteralInPrintlnRuleFactory(),
    )
}
```

`VisitorTableRegistry.get(version, rules)` resuelve el builder correcto para la
versión pedida y lo ejecuta — misma forma que `GrammarTableRegistry`/
`RuleTableRegistry` en el resto del proyecto.

## Las dos reglas implementadas para v1.0

### `identifier_format`

Config: un string, `"camel case"` o `"snake case"`. Aplica a cada `Leaf` de tipo
`IdentifierNode` en el árbol — nombres de variable introducidos por `let`, y
cada ocurrencia posterior de ese identificador. Un nombre que no matchea el
patrón configurado produce un diagnóstico `Lint` de severidad `Warning`,
apuntando exactamente al `span` de ese identificador.

### `mandatory-variable-or-literal-in-println`

Config: un booleano. Cuando es `true`, se chequea cada llamada a `println(...)`:
si su argumento es un `Node.Composite` (una expresión como `a + b`, no un
identificador o literal directo), se produce un diagnóstico `Lint` apuntando al
span de ese argumento. Cuando es `false`, la regla queda registrada pero nunca
reporta nada — esto refleja cómo la implementación de referencia permite
*desactivar* una regla por configuración sin sacarla del conjunto de reglas
activas.

## `PrintScriptLinter` — el punto de entrada

```kotlin
class PrintScriptLinter : Linter {
    override fun lint(version: String, nodes: Sequence<Node>, rules: Collection<Rule>): Sequence<Diagnostic> =
        sequence {
            when (val table = VisitorTableRegistry.get(version, rules)) {
                is Outcome.Ok -> yieldAll(lintNodes(nodes, table.value))
                is Outcome.Error -> yield(table.error)
            }
        }

    private fun collectErrorsRecursive(node: Node, table: VisitorTable): Sequence<Diagnostic> = sequence {
        for (visitor in table.visitors) {
            val outcome = node.accept(visitor, table)
            if (outcome is Outcome.Error) yield(outcome.error)
        }
        if (node is Node.Composite) {
            for (child in node.children) yieldAll(collectErrorsRecursive(child, table))
        }
    }
}
```

Acá está la diferencia arquitectónica clave respecto a todo lo construido hasta
ahora: `collectErrorsRecursive` **no se detiene** cuando un visitor reporta un
error. Junta ese diagnóstico y sigue probando el resto de los visitors contra el
mismo nodo, y después sigue recorriendo cada hijo sin importar lo anterior. Un
`ParseError` en el parser significa "no puedo entender lo que sigue, hay que
parar." Un diagnóstico `Lint` significa "acá hay una opinión sobre este pedazo
de código" — y puede, y debería, haber muchos a lo largo de un mismo archivo.

## Qué no hace este módulo (a propósito)

- **No corre reglas que no estén en la config.** Si `rules` no incluye una
  entrada para `"identifier_format"`, ese visitor directamente no se agrega a la
  tabla — ni siquiera se generan diagnósticos permisivos para esa regla.
- **No lee archivos de configuración.** `Collection<Rule>` llega ya parseado;
  convertir un archivo JSON/YAML de configuración en objetos `Rule` es
  responsabilidad de un futuro módulo CLI (`ConfigReader` en la implementación
  de referencia), no del linter en sí.
- **No valida tipos ni declaraciones.** Eso es trabajo del `Validator` — para
  cuando los nodos llegan al linter, se asume que ya son semánticamente válidos.

## Extender: agregar una tercera regla (`readInput`)

La implementación de referencia también incluye
`MandatoryIdentifierOrLiteralInReadInputRule`, idéntica en espíritu a la regla de
`println` pero apuntando a llamadas `readInput(...)`. No está incluida todavía
porque el parser no produce nodos `ReadInputPrimary` hasta que se agregue soporte
para v1.1 ahí. Cuando eso pase:

1. Agregar el `RuleType` en `RuleTypes.kt`.
2. Implementar `MandatoryVariableOrLiteralInReadInputVisitor` (misma forma que la
   de `println`, chequeando `ReadInputPrimary`/su `NodeType` en vez de
   `PrintlnStatementNode`).
3. Agregar su fábrica.
4. Registrar ambas en un objeto `PrintScriptV11` en `VisitorTableBuilders.kt`
   (armado como `PrintScriptV10.factories + mapOf(...)`, el mismo patrón
   aditivo ya usado para las versiones de `GrammarTableRegistry`), y agregar
   `"1.1" to lazy { PrintScriptV11 }` a `VisitorTableRegistry`.

Ninguna regla, visitor o fábrica existente necesita cambiar — es exactamente el
punto de extensión abierto/cerrado para el que se diseñó el patrón de tabla.