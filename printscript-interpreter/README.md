# PrintScript Interpreter — Guía de uso y arquitectura

El módulo `printscript-interpreter` ejecuta el árbol sintáctico abstracto (AST)
producido por `printscript-parser`. Mantiene el estado de las variables durante una
ejecución, evalúa expresiones, envía los resultados de `println` a una salida
inyectada y devuelve diagnósticos para los errores que encuentra.

```text
Sequence<Node>  →  PrintScriptInterpreter  →  efectos en OutputWriter
                                              + Sequence<Diagnostic>
```

El intérprete no recibe texto fuente ni tokens. Su entrada ya debe ser una
secuencia de nodos válidos generados por las etapas anteriores del pipeline:

```text
código fuente → Lexer → Token → Parser → Node → Interpreter
```

## Contrato público

La clase pública es `interpreter.PrintScriptInterpreter`, que implementa la
interfaz `Interpreter` de `printscript-api`:

```kotlin
fun interpret(
    version: String,
    nodes: Sequence<Node>,
    input: InputReader,
    output: OutputWriter,
    env: EnvReader,
): Sequence<Diagnostic>
```

- `version` selecciona la versión de PrintScript. Se aceptan `"1.0"` y `"1.1"`.
- `nodes` contiene las sentencias del programa en orden de ejecución.
- `input` abstrae la entrada del usuario: cada llamada a `read()` debe entregar
  una línea para una invocación de `readInput`.
- `output` recibe los textos producidos por `println` y los mensajes de `readInput`.
- `env` proporciona las variables del entorno consultadas por `readEnv`.
- el resultado es una secuencia de diagnósticos. Una ejecución correcta produce
  una secuencia vacía.

La secuencia devuelta es **lazy**: el programa se ejecuta a medida que el cliente
consume los diagnósticos. Para ejecutar todo el programa se debe consumir la
secuencia, por ejemplo con `toList()`. Además, se aplica `constrainOnce()`, por lo
que la secuencia solo puede recorrerse una vez.

```kotlin
val diagnostics = PrintScriptInterpreter()
    .interpret("1.0", nodes, input, output, env)
    .toList()
```

Cada invocación de `interpret` crea una tabla de símbolos nueva. El estado no se
comparte entre ejecuciones distintas, aunque se reutilice la misma instancia de
`PrintScriptInterpreter`.

## Funcionalidades soportadas

### Declaraciones `let`

Se admiten variables mutables de tipo `number`, `string` y, en 1.1, `boolean`, inicializadas o sin
inicializar:

```printscript
let count: number = 2;
let ratio: number;
let name: string = "Ada";
```

Al declarar una variable, el intérprete:

1. localiza el identificador y el tipo dentro del nodo;
2. rechaza nombres ya declarados en la misma ejecución;
3. evalúa la expresión inicial, si existe;
4. comprueba que el tipo del valor coincida con el declarado;
5. guarda el tipo y el valor en la tabla de símbolos.

Una declaración inválida no modifica la tabla. Las variables sin inicializador
quedan declaradas con valor nulo interno y no pueden leerse hasta que reciban una
asignación válida.

### Asignaciones

Una variable declarada con `let` puede reasignarse:

```printscript
let value: number;
value = 5 / 2;
```

La variable debe existir y el valor evaluado debe tener el mismo tipo declarado.
Si la expresión falla o hay incompatibilidad de tipos, se conserva el valor
anterior.

### `println`

`println` evalúa su expresión, convierte el valor a texto mediante `Value.format()`
y llama al `OutputWriter` con una secuencia que contiene ese texto:

```printscript
println("result: " + value);
```

El `OutputWriter` decide cómo materializar la salida: consola, archivo, memoria,
etc. El intérprete no agrega por sí mismo un carácter de salto de línea; la
semántica concreta depende del writer. Un nodo `println` sin expresión escribe una
cadena vacía.

Si la evaluación falla, no se escribe nada para esa sentencia y se devuelve el
diagnóstico correspondiente.

## Evaluación de expresiones

`ExpressionEvaluator` reconoce los siguientes nodos:

| Expresión | Comportamiento |
|---|---|
| literal numérico | conserva el `IntegerValue` o `FloatValue` generado por el parser |
| literal string | elimina comillas simples o dobles exteriores coincidentes |
| identificador | obtiene el valor actual desde la tabla de símbolos |
| expresión entre paréntesis | evalúa recursivamente el nodo interior |
| operación unaria | aplica `+` o `-` a un número |
| operación binaria | evalúa ambos operandos y aplica `+`, `-`, `*` o `/` |

El parser ya construye el AST respetando la precedencia y los paréntesis. El
intérprete simplemente recorre esa estructura de forma recursiva.

### Reglas numéricas

- `+`, `-` y `*` entre dos enteros producen un entero.
- cualquier operación que incluya un flotante produce un flotante.
- `/` siempre produce un flotante, incluso si ambos operandos son enteros.
- la división por cero produce un diagnóstico de categoría `Runtime`.
- los operadores unarios solo aceptan números y conservan la representación
  entera o flotante del operando.

Los cálculos se realizan internamente con `Float`. Por lo tanto, su precisión y
rango siguen los de ese tipo de Kotlin/JVM.

### Concatenación

El operador `+` concatena si al menos uno de los operandos es un string. Ambos
valores se convierten con `format()`:

```printscript
let x: number = 2;
println("value: " + x); // escribe "value: 2"
```

Los otros operadores requieren dos operandos numéricos.

## Funcionalidades de PrintScript 1.1

- `const` requiere un valor inicial y rechaza reasignaciones.
- `boolean` permite valores `true` y `false`.
- `if`/`else` evalúa la condición y ejecuta solo el bloque seleccionado, incluidos
  condicionales anidados. Un bloque omitido no consume entrada ni produce salida.
- `readInput(mensaje)` escribe el mensaje y obtiene una línea del `InputReader`.
- `readEnv(nombre)` obtiene un valor del `EnvReader`; una variable ausente produce
  un diagnóstico de ejecución.

Ambas lecturas requieren un argumento string, que también puede ser una expresión.
En una declaración o asignación, el resultado se convierte al tipo de destino.
En `println(readInput(...))` y `println(readEnv(...))`, el resultado es string.
Los strings conservan sus espacios; los números y booleanos se convierten después
de quitar espacios externos. Los booleanos aceptan `true` o `false`, y los números
deben ser finitos. Una conversión inválida detiene la ejecución.

La clase `RuntimeReader` concentra las lecturas y conversiones. `VersionSupport`
rechaza nodos exclusivos de 1.1 cuando se entregan programáticamente a la versión
1.0, antes de ejecutar la sentencia que los contiene.

## Tabla de símbolos

`InMemorySymbolTable` guarda las variables en un `MutableMap` indexado por nombre.
Cada entrada contiene:

- el `ValueType` declarado, que no cambia;
- el valor actual, que puede ser nulo hasta la primera inicialización;
- si la variable es mutable (`let`) o constante (`const`).

La tabla implementa tres operaciones internas: `find`, `declare` y `assign`. No
existen scopes anidados en esta versión: todas las variables pertenecen al único
scope de la ejecución.

## Diagnósticos y recuperación

Los errores esperables se representan como valores `Diagnostic`; no se lanzan
como excepciones. Todos los diagnósticos propios del intérprete tienen severidad
`Error`.

| Categoría | Cuándo se utiliza |
|---|---|
| `Configuration` | la versión solicitada no es `1.0` ni `1.1` |
| `Semantic` | variable duplicada, inexistente o no inicializada, tipos incompatibles, operador inválido, expresión/sentencia no soportada o nodo mal formado |
| `Runtime` | división por cero, conversión inválida o variable de entorno ausente |

Si la versión es incompatible, se emite un único diagnóstico de configuración y
los nodos no se consumen.

Ante un error semántico se informa el problema y se continúa con la siguiente
sentencia de nivel superior. Los errores `Runtime` detienen la ejecución. Los
cambios y las salidas anteriores al error se conservan, incluido el mensaje de
`readInput` cuando falla la conversión. Una declaración o asignación fallida no
reemplaza el valor de la variable.

Los mensajes actualmente producidos incluyen:

- `Unsupported PrintScript version: <version>`
- `Variable '<name>' is already declared`
- `Variable '<name>' is not declared`
- `Variable '<name>' has not been initialized`
- incompatibilidades al inicializar o asignar valores;
- operandos no numéricos para operadores aritméticos;
- `Division by zero`;
- nodos o operadores no soportados y nodos mal formados.

Los diagnósticos semánticos y de ejecución incluyen el `Span` del nodo afectado.

## Flujo interno

```text
PrintScriptInterpreter.interpret
  ├─ valida que version sea "1.0" o "1.1"
  ├─ crea InMemorySymbolTable
  ├─ crea RuntimeReader con input, output y env
  ├─ crea ExpressionEvaluator
  ├─ crea StatementExecutor con la salida
  └─ por cada Node, en orden
       ├─ LetDeclarationStatementNode / ConstDeclarationStatementNode → declare
       ├─ IfStatementNode             → conditional
       ├─ BlockNode                   → block
       ├─ AssignStatementNode         → assign
       ├─ PrintlnStatementNode        → print
       └─ cualquier otro tipo         → diagnóstico Semantic
```

`StatementExecutor` coordina los efectos de cada sentencia y delega todas las
expresiones a `ExpressionEvaluator`. `EvaluationResult` separa internamente los
resultados exitosos (`Success(Value)`) de los fallidos (`Failure(Diagnostic)`), de
modo que los errores se propagan sin excepciones.

## Integración con lexer y parser

Un consumidor que parte de código fuente debe ejecutar y validar las tres etapas.
En forma simplificada:

```kotlin
val tokenResults = PrintScriptLexer().lex("1.0", source.asSequence())
val tokens = tokenResults.map { result -> /* extraer Outcome.Ok o tratar Error */ }

val nodeResults = PrintScriptParser().parse("1.0", tokens)
val nodes = nodeResults.map { result -> /* extraer Outcome.Ok o tratar Error */ }

val diagnostics = PrintScriptInterpreter()
    .interpret("1.0", nodes, inputReader, outputWriter, envReader)
    .toList()
```

Lexer y parser devuelven sus propios errores mediante `Outcome`; estos deben
resolverse antes de entregar nodos al intérprete. El intérprete presupone la forma
de AST producida por el parser, aunque valida defensivamente los nodos y reporta
`Malformed '<tipo>' node` si su estructura no coincide con la esperada.

## Limitaciones actuales

El intérprete no implementa scopes anidados, operadores distintos de `+`, `-`,
`*` y `/`, ni versiones distintas de 1.0 y 1.1. Las restricciones sintácticas de
los condicionales y el límite actual del buffer del parser se resuelven en el
módulo parser.

## Estructura del módulo

```text
printscript-interpreter/
├── build.gradle
├── README.md
└── src/
    ├── main/kotlin/interpreter/
    │   ├── PrintScriptInterpreter.kt
    │   └── internal/
    │       ├── StatementExecutor.kt
    │       ├── ExpressionEvaluator.kt
    │       ├── RuntimeReader.kt
    │       ├── VersionSupport.kt
    │       ├── EvaluationResult.kt
    │       ├── SymbolTable.kt
    │       └── diagnostic/InterpreterDiagnostics.kt
    └── test/kotlin/interpreter/
        └── PrintScriptInterpreterTest.kt
```

El módulo depende en producción de `printscript-common` y `printscript-api`. Lexer
y parser son dependencias de test porque las pruebas de integración construyen el
AST desde código PrintScript real.

## Compilación y pruebas

Desde la raíz del repositorio:

```bash
./gradlew :printscript-interpreter:test
./gradlew :printscript-interpreter:build
```

Las pruebas existentes cubren declaraciones, asignaciones, aritmética y
precedencia, concatenación, división flotante, salida, preservación del estado
ante errores semánticos y rechazo de versiones no soportadas.
