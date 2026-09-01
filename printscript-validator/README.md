# PrintScript Validator

El módulo `validator` realiza el chequeo semántico y de tipos del AST ya
parseado, antes de que llegue al intérprete o al linter. Responde preguntas
como:

- ¿Esta variable fue declarada antes de usarse?
- ¿Se está reasignando una variable declarada con `let` (mutable) o intentando
  reasignar algo inmutable?
- ¿El valor asignado coincide con el tipo declarado (`number`, `string`)?
- ¿`+`, `-`, `*`, `/` se están aplicando a operandos de tipos compatibles?
- ¿Se está usando dos veces el mismo nombre de variable en el mismo scope?

No decide si el código es *sintácticamente* correcto (eso ya lo garantizó el
parser) ni evalúa el programa (eso es el intérprete). El validator es la etapa
que convierte "un AST bien formado" en "un AST que tiene sentido".

## Dónde se ubica en el pipeline

```text
Sequence<Char>
      |
      v
   Lexer --Token--> Parser --Node--> Validator --Node--> Linter/Interpreter
```

Como el `Parser`, el `Validator` puede fallar por nodo — un `Outcome.Error` en el
medio del stream es un error real que debería detener el resto del pipeline
(a diferencia del `Linter`, que junta advertencias de estilo sin cortar nada).

## API pública

```kotlin
interface Validator {
    fun validate(version: String, nodes: Sequence<Node>): Sequence<Outcome<Node, Diagnostic>>
}
```

Misma forma que `Parser`: por `version`, y devolviendo `Outcome` por elemento.
La diferencia con el linter no es de estilo, es conceptual — acá un error
*importa*, ahí abajo no se puede seguir razonando sobre un programa con un tipo
incompatible sin arriesgar reportar errores en cascada sin sentido.

## Por qué necesita `ContextVisitor`, no `Visitor` simple

El parser y el linter recorren el árbol nodo por nodo sin necesitar memoria de
lo que pasó antes. El validator sí la necesita: para saber si `x` en
`println(x)` es válido, tiene que recordar que unas líneas antes hubo un
`let x: number = 5;`. Esa memoria es una **tabla de símbolos**, y viaja de nodo
en nodo dentro de un `VisitorContext` inmutable — cada visita puede leerla y
devolver una versión actualizada, que se le pasa a la siguiente visita.

```kotlin
internal data class StaticSymbol(
    val name: String,
    val value: Value,
    val declaredType: ValueType,
    val declaredAt: Span,
    val isMutable: Boolean,
)

internal interface StaticSymbolTable {
    fun contains(key: String): Boolean
    fun get(key: String): Option<StaticSymbol>
    fun set(key: String, info: StaticSymbol): StaticSymbolTable  // devuelve una tabla NUEVA
}
```

`set` no muta la tabla — devuelve una copia con la entrada agregada. Esto es lo
que hace posible que la tabla de símbolos viva dentro de `VisitorContext`
(que ya vimos que es inmutable): cada declaración produce un
`VisitorContext` nuevo con la tabla actualizada, en vez de mutar algo compartido
entre nodos.

## `RuntimeValue`: qué hacer cuando falta información para seguir

Si `let x: number = calcularAlgoQueFalla();` falla al validar la expresión
asignada, igual conviene registrar `x` en la tabla — de lo contrario, la próxima
línea (`println(x);`) generaría un segundo error ("`x` no declarada") que no
tiene nada que ver con el problema real.

```kotlin
internal class RuntimeValue(override val type: ValueType) : Value {
    override fun format(): String = ""
}
```

`RuntimeValue` es un valor *placeholder*: no tiene un valor concreto, pero sí
sabe su tipo declarado. Las visitas de operación (`BinaryOperationVisitor`,
`UnaryOperationVisitor`) lo tratan como un caso especial — en vez de intentar
`apply()` sobre él (que fallaría, porque no hay un valor real), solo chequean
`supports(tipo)` para ver si el operador sería válido en principio, y devuelven
otro `RuntimeValue` con el tipo resultante. Así el error real se reporta una
sola vez, en el nodo que realmente falló.

## El modelo de operaciones: por qué `+` tiene cinco implementaciones distintas

```kotlin
interface BinaryValueOperation {
    val symbol: String
    val resultType: ValueType
    fun supports(lhs: ValueType, rhs: ValueType): Boolean
    fun apply(lhs: Value, rhs: Value): OperationResult
}

sealed interface OperationResult {
    data class Ok(val value: Value) : OperationResult
    data class Error(val message: String) : OperationResult
    data object Unsupported : OperationResult
}
```

En vez de un único `PlusOperation` con un `when` gigante adentro, hay varias
implementaciones chicas compitiendo por el mismo símbolo `+`:

- `AddIntegerOperation` — entero + entero
- `AddFloatOperation` — float + float
- `AddMixedNumericOperation` — entero + float (promociona a float)
- `AddStringOperation` — string + string (concatenación)
- `AddStringCoercionOperation` — string + number, o number + string (coerción a
  string)

`BinaryOperationVisitor` las prueba **en orden** y se queda con la primera cuyo
`apply()` devuelva `Ok`; si todas devuelven `Unsupported`, es un error de tipo
real (`TypeMismatch`). `OperationResult.Error` es distinto de `Unsupported`: es
para cuando el operador *sí* aplica pero falla en runtime — por ejemplo,
`5 / 0` es una división entre enteros válida en cuanto a tipos, pero inválida en
cuanto a valor.

Mismo patrón, otra vez, que `GrammarTable`/`VisitorTableBuilder`: muchas piezas
chicas e independientes, una tabla que las prueba en orden hasta encontrar la
que aplica.

## Los visitors de v1.0

| Visitor | Qué chequea |
|---|---|
| `NumberLiteralVisitor` | pasa el valor tal cual (ya viene tipado desde el parser) |
| `StringLiteralVisitor` | saca las comillas del lexeme (`'Hello'` → `Hello`) |
| `IdentifierVisitor` | busca el nombre en la tabla de símbolos; si no está, `UndefinedIdentifier` |
| `ParenthesizedExpressionVisitor` | delega en la expresión interna |
| `UnaryOperationVisitor` | prueba las `UnaryValueOperation` candidatas para el operador |
| `BinaryOperationVisitor` | prueba las `BinaryValueOperation` candidatas para el operador |
| `LetDeclarationVisitor` | chequea redeclaración, resuelve el tipo declarado, valida el tipo de la expresión asignada (si hay), registra el símbolo |
| `AssignmentVisitor` | chequea que la variable exista, que sea mutable, y que el tipo coincida |
| `PrintlnVisitor` | valida el argumento delegando en su propio visitor |

Todos siguen la misma forma: si el nodo no es el que le corresponde a este
visitor, devuelve `Outcome.Ok(NoneValue)` sin tocar el contexto — igual que en
el linter, cada visitor ignora todo lo que no es "suyo".

## `PrintScriptValidator` — el punto de entrada

```kotlin
class PrintScriptValidator : Validator {
    override fun validate(version: String, nodes: Sequence<Node>): Sequence<Outcome<Node, Diagnostic>> =
        sequence {
            when (val table = VisitorTableRegistry.get(version)) {
                is Option.Some -> yieldAll(validateNodes(nodes, table.value))
                is Option.None -> yield(Outcome.Error(ConfigurationError("Unsupported language version '$version'")))
            }
        }

    private fun validateNodes(nodes: Sequence<Node>, table: ContextVisitorTable) = sequence {
        var context = VisitorContext().register(StaticSymbolTable::class, DefaultStaticSymbolTable())
        val buffer = NodeBuffer(nodes)

        while (buffer.hasNext()) {
            val node = buffer.next()
            val visit = table.dispatch(node, context)
            context = visit.context               // el contexto avanza SIEMPRE

            when (val outcome = visit.outcome) {
                is Outcome.Ok -> yield(Outcome.Ok(node))
                is Outcome.Error -> yield(outcome) // pero el error SÍ se propaga
            }
        }
    }
}
```

La línea clave es `context = visit.context` fuera del `when`: pase lo que pase
con el `outcome` de este nodo, el contexto (la tabla de símbolos) sigue
actualizándose y viaja al siguiente nodo. Esto es intencional — si `let x:
number = "texto";` genera un `TypeMismatch`, igual conviene que `x` quede
registrada en la tabla (con `RuntimeValue`, como vimos), para que una línea
`println(x);` más abajo no genere un segundo error de "`x` no declarada" que
solo agregaría ruido sobre el problema real.

`NodeBuffer` cumple acá el mismo rol que `TokenBuffer` en el parser: itera la
`Sequence<Node>` con lookahead acotado, sin materializar el árbol completo en
memoria de una.

## Qué NO hace este módulo

- **No evalúa el programa.** `RuntimeValue`/`Value` acá son solo para chequeo de
  tipos; ejecutar código de verdad (sumar los enteros reales, imprimir por
  consola) es responsabilidad del `Interpreter`.
- **No opina sobre estilo.** Nombres de variables, formato de `println` — eso
  es el `Linter`, que además corre *después* del validator (el linter asume que
  el código ya es semánticamente válido).
- **No lee configuración.** A diferencia del linter, el validator no tiene
  reglas configurables por proyecto — sus chequeos (tipos, declaraciones,
  mutabilidad) son inherentes al lenguaje, no una preferencia de estilo.

## Un punto a verificar antes de portarlo

El validator de referencia distingue `IntegerValue` de `FloatValue` (ambos con
`type = NumberValueType`), justamente para poder resolver `AddIntegerOperation`
vs `AddFloatOperation` vs `AddMixedNumericOperation` según qué combinación
concreta de valores llegó. Si tu `NumberLiteralPrimary` del parser arma
actualmente un único `NumberValue` genérico (sin distinguir si el lexema tenía
punto decimal o no), vas a necesitar separarlo en `IntegerValue`/`FloatValue`
antes de portar `BinaryValueOperations.kt` tal cual — de lo contrario ninguna
operación matchearía nunca por tipo concreto.

## Extender: agregando v1.1 (`const`, `if`, `boolean`, `readEnv`, `readInput`)

Mismo patrón aditivo que en el resto del proyecto:

1. `ConstDeclarationVisitor` — igual que `LetDeclarationVisitor` pero registra
   el símbolo con `isMutable = false`.
2. `BooleanLiteralVisitor` — análogo a `NumberLiteralVisitor`/`StringLiteralVisitor`.
3. `IfStatementVisitor`, `BlockVisitor` — necesitan manejar *scopes anidados* en
   la tabla de símbolos (una variable declarada dentro de un `if` no debería
   sobrevivir afuera de él), lo cual requiere que `VisitorContext` pueda
   apilar/desapilar tablas, no solo reemplazarlas.
4. `ReadEnvVisitor`, `ReadInputVisitor` — validan que el argumento sea del tipo
   esperado (string para el nombre de la variable de entorno / el prompt).
5. Registrar todo en un `PrintScriptV11` que extiende `PrintScriptV10.visitors`,
   y agregar `"1.1"` a `VisitorTableRegistry` — sin tocar ninguno de los
   visitors de v1.0.