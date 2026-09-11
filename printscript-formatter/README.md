# PrintScript Formatter

El módulo `formatter` reescribe el espaciado, los saltos de línea y (en v1.1) la
indentación de un AST ya parseado y validado, según reglas configurables por
proyecto. Responde preguntas como:

- ¿Debe haber espacio alrededor de `=`?
- ¿Cuántos espacios van después de `:` en una declaración?
- ¿Cuántos saltos de línea van después de un `println(...)`?
- ¿La llave de un `if` va en la misma línea que la condición, o en la siguiente?
- ¿Cuántos espacios de indentación lleva el contenido de un bloque `if`?

No decide si el código es válido — eso ya pasó en el parser y el validator. El
formatter transforma un AST *válido* en su representación de texto canónica,
según la configuración del proyecto.

## Dónde se ubica en el pipeline

```text
Sequence<Char>
      |
      v
   Lexer --Token--> Parser --Node--> Validator --Node--> Formatter --Doc--> archivo formateado
                                                              ^
                                                              |
                                                    Collection<Rule> (config)
```

Como el `Linter`, recibe `Collection<Rule>` con la configuración del proyecto.
Como el `Validator`, puede fallar por configuración inválida (`Outcome.Error`).
Pero a diferencia de los dos, **su salida no es un `Node`, es un `Doc`** — ya no
hay más transformaciones estructurales después del formatter, esto es texto
final.

## API pública

```kotlin
interface Formatter {
    fun format(version: String, nodes: Sequence<Node>, rules: Collection<Rule>): Sequence<Outcome<Doc, Diagnostic>>
}
```

## El modelo `Doc` (en `common`)

```kotlin
data class Doc(
    val text: String,
    val span: Span,
    val leading: Collection<Trivia>,
    val trailing: Collection<Trivia>,
) {
    fun format(): String = leading.joinToString("") { it.lexeme } + text + trailing.joinToString("") { it.lexeme }
}
```

Un `Doc` es la versión "lista para imprimir" de un nodo: su texto final más su
trivia (ya reescrita según las reglas), en un único valor cuyo `.format()`
produce exactamente lo que va a escribirse a disco. Es deliberadamente el
último eslabón de la cadena — nada después de esto vuelve a tocar la estructura
del AST.

## Dos "tipos de resultado" internos: `NodeValue` y `DocValue`

Cada regla del formatter, al procesar un nodo, puede devolver dos cosas
distintas envueltas en `Outcome.Ok`:

```kotlin
internal data class NodeValue(val value: Node) : Value   // "seguí transformando esto"
internal data class DocValue(val value: Doc) : Value     // "esto ya está terminado"
```

- **`NodeValue`**: la regla modificó la trivia o la estructura del nodo (por
  ejemplo, le sacó los espacios sobrantes alrededor de `=`), pero el nodo
  todavía tiene que pasar por el resto de las reglas activas.
- **`DocValue`**: ya no hay nada más para transformar; esto es el resultado
  final. `PrintScriptFormatter` lo usa tal cual; si en cambio nunca llega
  ningún `DocValue`, convierte el `Node` final a `Doc` con `ToDoc.kt`
  (`node.toDoc()`), que simplemente serializa el nodo con su trivia actual.

## El patrón de dispatch más importante de este módulo: **cadena**, no "todos" ni "el primero que matchee"

Ya viste tres formas distintas de recorrer/combinar reglas en el proyecto:

| Módulo | Patrón de dispatch |
|---|---|
| Parser (`GrammarTable`) | *el primero que matchee gana* (longest match) |
| Linter (`PrintScriptLinter`) | *todos*, se acumulan todos los diagnósticos, nunca se corta |
| Validator (`ContextVisitorTable.dispatch`) | *el primero que falle corta todo* |
| **Formatter (`FormatterContextVisitorTable`)** | **cadena: cada regla activa transforma el nodo y se lo pasa a la siguiente** |

```kotlin
internal class FormatterContextVisitorTable(
    override val visitors: Collection<ContextVisitor>,
) : ContextVisitorTable {
    override fun dispatch(node: Node, context: VisitorContext): VisitResult {
        var currentContext = context
        var currentNode = node

        for (visitor in visitors) {
            val visit = currentNode.accept(visitor, this, currentContext)
            if (visit.outcome is Outcome.Error) return visit

            currentContext = visit.context
            val value = (visit.outcome as? Outcome.Ok)?.value
            if (value is NodeValue) currentNode = value.value
        }

        return VisitResult(Outcome.Ok(DocValue(currentNode.toDoc())), currentContext)
    }
}
```

Esto tiene sentido apenas lo pensás: el espaciado alrededor de `=` y la cantidad
de saltos de línea después de un `println` **no son mutuamente excluyentes** —
un mismo nodo puede necesitar que le apliquen las dos reglas, una después de la
otra, cada una modificando lo que dejó la anterior. Por eso el formatter no
"elige una regla" como el parser, ni "prueba todas por separado" como el
linter: **encadena** las reglas activas, cada una recibiendo el nodo ya
modificado por la anterior.

## `TriviaManipulator`: la caja de herramientas que usa cada regla

Casi todas las reglas de v1.0 hacen lo mismo en el fondo: sacar cierta trivia de
donde está y ponerla donde debería estar. `TriviaManipulator` centraliza esa
manipulación para que ninguna regla tenga que reimplementarla:

```kotlin
internal object TriviaManipulator {
    fun extractLeading(node: Node, type: TriviaType): Pair<Node, List<Trivia>>
    fun extractTrailing(node: Node, type: TriviaType): Pair<Node, List<Trivia>>
    fun addLeading(node: Node, trivia: List<Trivia>): Node
    fun addTrailing(node: Node, trivia: List<Trivia>): Node
    fun removeLeading(node: Node, type: TriviaType): Node
    fun removeTrailing(node: Node, type: TriviaType): Node
}
```

Cada función recorre recursivamente hasta el `Leaf` correspondiente (el primero
para `leading`, el último para `trailing` — porque un `Node.Composite` no tiene
trivia propia, solo la heredan sus hojas extremas) y devuelve una copia
inmutable del árbol con el cambio aplicado. Ningún nodo se muta nunca in place
— es el mismo principio de inmutabilidad que ya viste en `StaticSymbolTable` o
`VisitorContext`.

## `NodeTransformer` / `NodeTraversal`: bajar el cambio a los hijos

Una regla que decide algo sobre un nodo `Composite` (por ejemplo,
"después de este `println` van 2 saltos de línea") necesita también que sus
*hijos* terminen de procesarse con el resto de la tabla antes de decidir el
resultado final. `NodeTransformer.transformRecursive` hace exactamente eso:
aplica `table.dispatch(...)` a cada hijo recursivamente, y `NodeTraversal`
provee utilidades de recorrido de solo lectura (por ejemplo, `endsWith` para
saber si el último token de un nodo es de cierto `NodeType`, útil para
saber si un `if` termina en `}`).

## Estado que viaja entre nodos: `IndentState`, `LineBreakState`, `PrintlnState`

Igual que el `Validator` necesitaba una tabla de símbolos viajando en
`VisitorContext`, varias reglas del formatter necesitan recordar algo del nodo
anterior:

- **`PrintlnState`** — recuerda si el nodo anterior fue un `println`, para
  poder normalizar cuántos saltos de línea van *después* de él (mirando el
  nodo siguiente, no el propio).
- **`IndentState`** (v1.1) — el nivel de indentación actual, que sube al entrar
  a un bloque `if` y baja al salir.
- **`LineBreakState`** — saltos de línea pendientes de aplicar, más una bandera
  de "es el primer nodo del archivo" (para no forzar una línea en blanco antes
  del primer statement).

Todas siguen el mismo patrón: `context.get(EstadoX::class)`, se calcula el
nuevo estado, `context.register(EstadoX::class, nuevoEstado)` — inmutable,
igual que en el validator.

## Las 8 reglas de v1.0

| `RuleType.signature` | Qué hace |
|---|---|
| `enforce-spacing-around-equals` | fuerza exactamente un espacio a cada lado de `=` |
| `enforce-no-spacing-around-equals` | fuerza cero espacios a cada lado de `=` |
| `enforce-spacing-before-colon-in-declaration` | espacio (o no) antes del `:` en `let x: number` |
| `enforce-spacing-after-colon-in-declaration` | espacio (o no) después del `:` |
| `mandatory-single-space-separation` | colapsa cualquier trivia de espacio a exactamente un espacio simple entre tokens que lo necesiten |
| `mandatory-space-surrounding-operations` | espacio alrededor de `+ - * /` |
| `mandatory-line-break-after-statement` | fuerza salto de línea al final de cada sentencia |
| `line-breaks-after-println` | cantidad configurable de saltos de línea después de cada `println(...)` |

`SpacingAroundEqualsRule`/`NoSpacingAroundEqualsRule` son mutuamente
excluyentes por diseño de configuración (no por código): si activás las dos a
la vez, la cadena de dispatch simplemente aplica ambas en orden y la segunda
pisa el resultado de la primera — la validación de "no actives las dos" es
responsabilidad de quien arma el archivo de config, no del formatter.

## Reglas de v1.1: `if`, indentación y llaves

Estas tres reglas son cualitativamente distintas de las de v1.0: ya no alcanza
con mover trivia de un lado a otro, porque necesitan decidir **estructura**
(cuántos niveles de indentación lleva cada línea dentro de un bloque) y
**posición relativa entre nodos hermanos** (si la `{` va pegada a la condición
o en su propia línea).

| `RuleType.signature` | Qué hace |
|---|---|
| `indent-inside-if` | cantidad configurable de espacios de indentación para cada sentencia dentro de un bloque `if`/`else` |
| `if-brace-same-line` | fuerza que `{` quede en la misma línea que la condición del `if` |
| `if-brace-below-line` | fuerza que `{` quede en su propia línea, debajo de la condición |

`IndentsInsideIfVisitor` es la más grande del módulo: lee `IndentState` del
contexto, lo incrementa al entrar al `BlockNode` del `if`, recorre cada
sentencia hija reescribiendo su trivia `leading` con `indents * currentLevel`
espacios, y lo restaura al salir — para que un `if` anidado dentro de otro `if`
acumule niveles correctamente en vez de reiniciar a cero.

## De la config a la tabla: mismo patrón que en el linter, con un matiz

```kotlin
internal interface ContextVisitorTableBuilder {
    val factories: Map<String, ContextVisitorFactory>

    fun build(rules: Collection<Rule>): Outcome<FormatterContextVisitorTable, Diagnostic> {
        val visitors = mutableListOf<ContextVisitor>()
        for (rule in rules) {
            val factory = factories[rule.signature] ?: continue
            try {
                visitors.add(factory.create(rule))
            } catch (e: Exception) {
                return Outcome.Error(ConfigurationError("Rule '${rule.signature}' value has invalid type '${rule.value.type()}'"))
            }
        }
        return Outcome.Ok(FormatterContextVisitorTable(visitors))
    }
}
```

Mismo mecanismo que `VisitorTableBuilder` del linter (traducir `Rule` crudo a
`Visitor` configurado vía `ContextVisitorFactory`), con un matiz: acá el
`try/catch` alrededor del cast (`rule.value as BooleanRuleValue`, etc.) es
explícito y deliberado — es el único punto de todo el módulo donde se permite
capturar una excepción genérica, precisamente porque es la frontera entre "dato
crudo sin tipar" y "dato con el tipo que cada regla espera", y un fallo ahí
tiene que convertirse en un `Diagnostic` de configuración, no propagarse.

```kotlin
internal object PrintScriptV10 : ContextVisitorTableBuilder {
    override val factories = mapOf(
        NoSpacingAroundEqualsRule.signature to NoSpacingAroundEqualsVisitorFactory(),
        SpacingAroundEqualsRule.signature to SpacingAroundEqualsVisitorFactory(),
        // ... el resto de las 8 reglas de v1.0
    )
}

internal object PrintScriptV11 : ContextVisitorTableBuilder {
    override val factories = PrintScriptV10.factories + mapOf(
        IndentsInsideIfBlockRule.signature to IndentsInsideIfVisitorFactory(),
        IfBraceSameLineRule.signature to IfBraceSameLineVisitorFactory(),
        IfBraceBelowLineRule.signature to IfBraceBelowLineVisitorFactory(),
    )
}
```

Mismo patrón aditivo que en todos los módulos anteriores: v1.1 reutiliza
`PrintScriptV10.factories` y solo agrega lo nuevo — ningún visitor de v1.0
cambia.

## `PrintScriptFormatter` — el punto de entrada

```kotlin
class PrintScriptFormatter : Formatter {
    override fun format(version: String, nodes: Sequence<Node>, rules: Collection<Rule>) =
        sequence {
            when (val table = VisitorTableRegistry.get(version, rules)) {
                is Outcome.Ok -> yieldAll(formatNodes(nodes, table.value))
                is Outcome.Error -> yield(Outcome.Error(table.error))
            }
        }

    private fun formatNodes(nodes: Sequence<Node>, table: ContextVisitorTable) = sequence {
        var context = VisitorContext()
        for (node in nodes) {
            val visit = table.dispatch(node, context)
            context = visit.context
            when (val outcome = visit.outcome) {
                is Outcome.Ok -> yield(
                    Outcome.Ok(if (outcome.value is DocValue) outcome.value.value else node.toDoc())
                )
                is Outcome.Error -> yield(outcome)
            }
        }
    }
}
```

El `context` viaja de nodo en nodo igual que en el validator (así
`PrintlnState`/`IndentState` recuerdan lo que pasó en la sentencia anterior), y
cada nodo produce un `Doc` — ya sea el que armó la cadena de reglas
(`DocValue`), o el fallback de serializar el nodo tal cual llegó
(`node.toDoc()`) si ninguna regla lo tocó.

## Qué NO hace este módulo

- **No valida nada.** Si le llega un AST con un error de tipos, no es su
  problema detectarlo — el `Validator` ya debería haber cortado antes.
- **No escribe a disco.** Produce `Sequence<Outcome<Doc, Diagnostic>>`; volcar
  cada `Doc.format()` a un archivo es responsabilidad de un futuro módulo CLI.
- **No decide un estilo por defecto propio.** Sin ninguna `Rule` activa, el
  formatter no reescribe nada — cada nodo pasa por la cadena vacía y se
  serializa tal cual (`node.toDoc()`), preservando exactamente la trivia
  original del lexer.

## Extendiendo el formatter

Agregar una regla nueva (de v1.1 en adelante) sigue siempre la misma receta:

1. Agregar el `RuleType` en `RuleTypes.kt`.
2. Implementar el `ContextVisitor`: devolver `Outcome.Ok(NoneValue)` sin tocar
   nada si el nodo no es el que le corresponde; si lo es, usar
   `TriviaManipulator` (o manipulación estructural directa del `Composite`,
   como en `IndentsInsideIfVisitor`) y devolver `Outcome.Ok(NodeValue(nodo
   modificado))`.
3. Implementar su `ContextVisitorFactory`, haciendo el cast del `RuleValue`
   correspondiente.
4. Agregar ambos al `factories` map de la tabla de la versión que corresponda.

Ninguna regla existente necesita cambiar — es, otra vez, el mismo punto de
extensión abierto/cerrado que ya viste en el parser, el linter y el validator.