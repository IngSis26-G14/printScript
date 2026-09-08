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

- `version` selecciona la versión de PrintScript. Actualmente solo se acepta
  exactamente `"1.0"`.
- `nodes` contiene las sentencias del programa en orden de ejecución.
- `input` abstrae la entrada del usuario. Se recibe para cumplir el contrato de la
  API, pero la versión actual todavía no implementa `readInput` y no lo consulta.
- `output` recibe los textos producidos por `println`.
- `env` abstrae variables del entorno. También está preparado para futuras
  expresiones `readEnv`, pero actualmente no se consulta.
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

Se admiten variables mutables de tipo `number` y `string`, inicializadas o sin
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

## Tabla de símbolos

`InMemorySymbolTable` guarda las variables en un `MutableMap` indexado por nombre.
Cada entrada contiene:

- el `ValueType` declarado, que no cambia;
- el valor actual, que puede ser nulo hasta la primera inicialización.

La tabla implementa tres operaciones internas: `find`, `declare` y `assign`. No
existen scopes anidados en esta versión: todas las variables pertenecen al único
scope de la ejecución.

## Diagnósticos y recuperación

Los errores esperables se representan como valores `Diagnostic`; no se lanzan
como excepciones. Todos los diagnósticos propios del intérprete tienen severidad
`Error`.

| Categoría | Cuándo se utiliza |
|---|---|
| `Configuration` | la versión solicitada no es `1.0` |
| `Semantic` | variable duplicada, inexistente o no inicializada, tipos incompatibles, operador inválido, expresión/sentencia no soportada o nodo mal formado |
| `Runtime` | división por cero |

Si la versión es incompatible, se emite un único diagnóstico de configuración y
los nodos no se consumen.

Para los errores de una sentencia, el intérprete emite un diagnóstico y continúa
con el nodo siguiente. Esto permite informar varios problemas en una ejecución.
Los efectos realizados antes del error se conservan; la operación inválida en sí
no cambia variables ni genera salida. Por ejemplo, una redeclaración fallida no
reemplaza el valor original.

Los mensajes actualmente producidos incluyen:

- `Unsupported PrintScript version: <version>`
- `Variable '<name>' is already declared`
- `Variable '<name>' is not declared`
- `Variable '<name>' has not been initialized`
- incompatibilidades al inicializar o asignar valores;
- operandos no numéricos para operadores aritméticos;
- `Division by zero`;
- nodos o operadores no soportados y nodos mal formados.

Los diagnósticos del intérprete no incluyen un `Span`; los mensajes identifican el
problema por su descripción y, cuando corresponde, por el nombre de variable.

## Flujo interno

```text
PrintScriptInterpreter.interpret
  ├─ valida que version == "1.0"
  ├─ crea InMemorySymbolTable
  ├─ crea ExpressionEvaluator
  ├─ crea StatementExecutor con las dependencias de I/O
  └─ por cada Node, en orden
       ├─ LetDeclarationStatementNode → declare
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

Aunque `printscript-common` ya define tipos de nodo para futuras características,
esta implementación de la versión 1.0 no ejecuta:

- booleanos ni el tipo `boolean`;
- declaraciones `const`;
- condicionales, bloques o scopes anidados;
- `readInput`;
- `readEnv`;
- operadores distintos de `+`, `-`, `*` y `/`;
- versiones del lenguaje distintas de `1.0`.

Recibir uno de esos nodos directamente genera un diagnóstico de expresión o
sentencia no soportada. `InputReader` y `EnvReader` ya forman parte del constructor
interno de ejecución para que esas capacidades puedan incorporarse sin cambiar el
contrato público.

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
