# PrintScript Parser — Guía de arquitectura

Este documento explica cómo funciona el módulo `printscript-parser`, pensado para
alguien que se suma al proyecto y necesita entender el diseño antes de tocar código.

## 1. ¿Qué hace este módulo?

Convierte una secuencia de `Token` (producida por el `Lexer`) en una secuencia de
nodos de AST (`Node`), o en errores de parseo (`Diagnostic`) cuando el código no
respeta la gramática del lenguaje.

```
Sequence<Token>  →  [ Parser ]  →  Sequence<Outcome<Node, Diagnostic>>
```

Puntos clave de ese contrato:

- **Es lazy / streaming.** Tanto la entrada (`Sequence<Token>`) como la salida
  (`Sequence<Outcome<...>>`) son secuencias perezosas. El parser no carga todo el
  archivo en memoria: va consumiendo tokens y produciendo nodos a demanda. Esto es
  un requisito explícito del TP (archivos fuente demasiado grandes para memoria).
- **No lanza excepciones para errores de sintaxis.** Un error de parseo es un
  valor (`Outcome.Error(Diagnostic)`), no una excepción. Las excepciones quedan
  reservadas para bugs internos del parser, no para código de usuario inválido.
- **Depende de una versión del lenguaje.** `parse(version, tokens)` recibe un
  string (`"1.0"`, `"1.1"`, ...) porque el lenguaje va a crecer con el tiempo y
  cada versión tiene su propia gramática.

## 2. Los tipos base que usa (de `printscript-core`)

Antes de leer el parser en sí, hace falta tener claros estos tipos compartidos:

### `Token`

```kotlin
data class Token(
    val type: TokenType,
    val lexeme: String,
    val span: Span,
    val leading: Collection<Trivia> = emptyList(),
    val trailing: Collection<Trivia> = emptyList(),
)
```

- `type`: la categoría del token (`LetToken`, `IdentifierToken`, `PlusToken`, ...).
  Es lo que el parser mira para decidir qué regla gramatical aplica.
- `lexeme`: el texto crudo tal cual apareció en el código fuente (`"5"`, `"x"`,
  `"hola"`). No está convertido a `Int`/`String` todavía — esa conversión la hace
  el parser al construir el `Value` del nodo.
- `span`: dónde está ese token en el archivo (ver más abajo). Es lo que permite
  reportar errores con posición exacta.
- `leading` / `trailing`: espacios, tabs, saltos de línea o comentarios pegados
  al token. El parser los ignora al aplicar la gramática, pero se conservan para
  que el Formatter pueda usarlos más adelante. En la v1.0 quedan vacíos.

### `Span` / `Position`

```kotlin
data class Position(val line: Int, val column: Int, val index: Int)
data class Span(val start: Position, val end: Position)
```

Un `Span` es un **rango**, no un punto: inicio y fin. Esto se compone hacia
arriba — cuando el parser arma un `Node` a partir de varios tokens, su `Span` es
`Span(primerToken.span.start, ultimoToken.span.end)`. Así, un error sobre una
sentencia completa (no solo un token) puede señalar el rango exacto que ocupa.

### `Node` (el AST)

```kotlin
sealed class Node {
    abstract val type: NodeType
    abstract val span: Span
    data class Leaf(override val type: NodeType, val value: Value, override val span: Span, ...) : Node()
    data class Composite(override val type: NodeType, val children: Collection<Node>, override val span: Span) : Node()
}
```

Solo dos formas de nodo:

- **`Leaf`**: un nodo hoja con un valor concreto (un literal, un identificador).
- **`Composite`**: un nodo con hijos (una sentencia `let`, una operación binaria).

No hay una clase por cada construcción del lenguaje (`LetNode`, `PlusNode` como
clases separadas) — en cambio, `NodeType` (un `data object` por construcción)
es lo que distingue "qué es" cada nodo, y `Leaf`/`Composite` es "cómo está
armado". Esto mantiene el árbol homogéneo y fácil de recorrer genéricamente.

### `Diagnostic` / `Outcome`

```kotlin
interface Diagnostic { val message: String; val severity: Severity; val category: Category }
sealed interface Outcome<out T, out E> {
    data class Ok<out T>(val value: T) : Outcome<T, Nothing>
    data class Error<out E>(val error: E) : Outcome<Nothing, E>
}
```

`Outcome<T, E>` es el "either" del proyecto: en vez de lanzar excepciones, cada
operación que puede fallar devuelve `Ok(valor)` o `Error(diagnóstico)`. Todo el
parser está escrito en términos de este tipo — no vas a encontrar `try/catch`
para errores de sintaxis en ningún lado.

## 3. El flujo completo: de tokens a nodos

```
PrintScriptParser.parse(version, tokens)
        │
        ├─ 1. GrammarTableRegistry.get(version)  →  Option<GrammarTable>
        │       (si la versión no existe: Outcome.Error(ConfigurationError))
        │
        └─ 2. TokenBuffer(tokens)  +  NodeScanner
                loop mientras haya tokens:
                    scanner.scan(buffer, table)  →  NodeScan
                        ├─ Empty  → no quedan tokens, terminar
                        ├─ Error  → yield Outcome.Error(ParseError), terminar
                        └─ Ok     → yield Outcome.Ok(node), buffer.advance(consumidos)
```

### `TokenBuffer` — la ventana deslizante sobre la secuencia

```kotlin
internal class TokenBuffer(tokens: Sequence<Token>) {
    private val inner = ArrayDeque<Token>()   // hasta 64 tokens de lookahead
    fun peek(n: Int = 1): Collection<Token>
    fun advance(n: Int = 1)
    fun hasNext(n: Int = 1): Boolean
}
```

Es el mecanismo que permite que el parser sea streaming: mantiene un buffer
acotado (64 tokens de lookahead) en vez de materializar toda la secuencia.
`peek(n)` mira los próximos `n` tokens sin consumirlos, `advance(n)` los
descarta y rellena el buffer con más tokens de la fuente original.

### `NodeScanner` — probar de a un token más hasta encontrar el mejor match

El scanner no sabe nada de gramática específica; su trabajo es genérico:

1. Prueba `table.dispatchStatement(slice)` con `slice` = 1 token.
2. Si matchea, guarda ese resultado como candidato y **agranda el slice** para
   ver si con un token más el match consume incluso más (longest match wins).
3. Si falla, guarda el mejor error visto y también agranda el slice (una
   sentencia inválida puede necesitar más tokens para determinar el mensaje de
   error más preciso).
4. Se detiene cuando agrandar el slice deja de mejorar el resultado, y devuelve
   el mejor nodo encontrado o, si no hubo ninguno, el mejor error.

Este diseño ("longest match", probando tamaños de slice crecientes) es lo que
le permite al parser no tener que saber de antemano cuántos tokens ocupa cada
sentencia — se lo pregunta a la gramática.

## 4. La gramática: `GrammarTable` y las piezas que la componen

### La interfaz `Grammar`

```kotlin
internal interface Grammar {
    val type: NodeType
    fun match(tokens: List<Token>, table: GrammarTable): Outcome<GrammarMatch, GrammarFail>
}
```

Cada regla gramatical (cada `Statement`, `Primary` o `Expression`) es una clase
chica que implementa `Grammar.match`: recibe una lista de tokens candidatos y
devuelve o bien un `GrammarMatch(node, consumidos)` o un `GrammarFail(mensaje,
categoría, consumidos)`.

Tres roles, todos son `Grammar` con distinto nombre semántico:

| Interfaz | Qué reconoce | Ejemplos en v1.0 |
|---|---|---|
| `Statement` | Una sentencia completa (nivel superior) | `AssignStatement`, `LetDeclarationStatement`, `PrintlnStatement` |
| `Primary` | Un valor "atómico" dentro de una expresión | `IdentifierPrimary`, `NumberLiteralPrimary`, `StringLiteralPrimary`, `ParenthesizedPrimary`, `UnaryOperationPrimary` |
| `Expression` | Combinación de primaries con operadores | `BinaryOperationExpression` |

### `GrammarTable` — el árbitro entre reglas que compiten

```kotlin
internal interface GrammarTable {
    val statements: Collection<Statement>
    val expressions: Collection<Expression>
    val primaries: Collection<Primary>

    fun dispatchStatement(tokens: List<Token>): Outcome<GrammarMatch, GrammarFail>
    // dispatchExpression, dispatchPrimary: misma idea
}
```

`dispatch` prueba **todas** las reglas de la colección contra los mismos tokens
y se queda con la que consumió más tokens (`bestMatch`). Si ninguna matchea, se
queda con el error que llegó más lejos (`lastFail`) — así el mensaje de error
es el más específico posible, no simplemente "no matcheó nada".

Este es el mismo patrón que `VisitorTable` usa para nodos: en vez de un único
parser monolítico con un `when` gigante, una colección de piezas chicas
independientes y una tabla que arbitra entre ellas. Agregar una construcción
nueva al lenguaje = agregar una clase `Grammar` nueva a la lista, sin tocar las
demás (abierto/cerrado).

### Cómo se recorre una expresión: primary → expression

`BinaryOperationExpression` es la única `Expression` de la v1.0 e implementa
**precedencia de operadores** vía el mapa `Map<TokenType, BinaryOperator>`:

```kotlin
BinaryOperationExpression(mapOf(
    DivideToken   to BinaryOperator(DivideNode, 2),
    MultiplyToken to BinaryOperator(MultiplyNode, 2),
    PlusToken     to BinaryOperator(PlusNode, 1),
    MinusToken    to BinaryOperator(MinusNode, 1),
))
```

Número más alto = se evalúa primero (`*` y `/` antes que `+` y `-`). El
algoritmo delega en `table.dispatchPrimary` para resolver los operandos (que a
su vez puede recursar en `ParenthesizedPrimary` → `dispatchExpression` para
paréntesis anidados) y va combinando resultados respetando la precedencia.

### `GrammarTableRegistry` — una tabla por versión del lenguaje

```kotlin
internal object GrammarTableRegistry {
    private val tables: Map<String, Lazy<GrammarTable>> = mapOf(
        "1.0" to lazy { PrintScriptV10 },
        // "1.1" to lazy { PrintScriptV11 },  ← se agrega cuando exista
    )
    fun get(version: String): Option<GrammarTable>
}
```

Cada versión del lenguaje es simplemente **una lista distinta de
statements/primaries/expressions**. Extender el lenguaje (agregar `if`,
`const`, booleanos, etc.) no modifica ninguna clase existente: se define un
`PrintScriptV11` que reutiliza lo de `PrintScriptV10` y le suma piezas nuevas.

## 5. Manejo de errores: `GrammarFail` → `ParseError`

```kotlin
internal data class GrammarFail(val message: String, val category: Category, val consumed: Int)
internal data class ParseError(
    override val message: String,
    override val category: Category,
    val span: Span,
    override val severity: Severity = Error,
) : Diagnostic
```

`GrammarFail` es el error "interno" que produce una regla gramatical individual
— todavía no tiene una posición concreta, solo un mensaje, una categoría
(`MissingSemicolon`, `InvalidStatement`, etc.) y cuántos tokens llegó a
consumir antes de fallar. `PrintScriptParser.buildParseError` es quien lo
convierte en un `ParseError` con `Span` real, usando:

- el token donde ocurrió el fallo, si existe, o
- el final del último token válido, si el error es "falta algo al final del
  archivo" (por ejemplo, una sentencia sin `;` justo antes del EOF).

`ConfigurationError` es un `Diagnostic` distinto, para un problema distinto:
versión de lenguaje no soportada. No tiene `Span` porque no corresponde a un
punto del archivo.

## 6. Ejemplo completo, paso a paso

Para `let x: number = 5;`:

1. `GrammarTableRegistry.get("1.0")` → `PrintScriptV10`.
2. `TokenBuffer` arranca con los tokens `[Let, Identifier(x), Colon,
   NumberType, Assign, NumberLiteral(5), Semicolon, ...]`.
3. `NodeScanner.scan` prueba `dispatchStatement` con slices crecientes.
   `LetDeclarationStatement.match` es la única que matchea, y consume los 7
   tokens de la sentencia completa.
4. Se produce `Outcome.Ok(Node.Composite(LetDeclarationStatementNode, [...],
   span))`, con hijos que incluyen el `Leaf` del identificador y el resultado
   de parsear la expresión `5` (vía `dispatchExpression` → `dispatchPrimary` →
   `NumberLiteralPrimary`).
5. `buffer.advance(7)` y el loop continúa con la siguiente sentencia.

Si en cambio el código fuera `let x: number = 5` (sin `;`):

1. `LetDeclarationStatement.match` llega hasta el final esperando un
   `SemicolonToken` y no lo encuentra.
2. Devuelve `Outcome.Error(GrammarFail("Expected ';'", MissingEndOfLine, 6))`.
3. Como no hay más tokens que probar, el scanner devuelve ese error.
4. `buildParseError` no encuentra un token en la posición 6 (no hay más
   tokens), así que arma un `Span` puntual en el final del último token válido
   (el `5`) — indicando exactamente dónde falta el `;`.

## 7. Cómo extender esto (para cuando llegue la v1.1 u otras features)

- **Nueva construcción del lenguaje** (ej. `if`): crear una clase que
  implemente `Statement` (o `Primary`/`Expression` según corresponda),
  agregarla a la lista de un nuevo `GrammarTable` de versión. No se toca
  ninguna clase existente.
- **Nuevo tipo de dato** (ej. `boolean`): agregar el `TokenType`/`NodeType`
  correspondiente en `core`, y sumarlo a los mapas que ya reciben tipos como
  parámetro (ej. `LetDeclarationStatement(mapOf(NumberTypeToken to
  NumberTypeNode, ..., BooleanTypeToken to BooleanTypeNode))`).
- **Nueva categoría de error**: agregar un `data object` a
  `parser/internal/model/category/Categories.kt`.

## 8. Qué NO hace este módulo (para no confundirse buscándolo acá)

- No ejecuta código (`Value`, `Visitor`, `VisitorTable` son tipos que `Node`
  referencia porque el AST los necesita en su firma, pero la lógica de
  evaluarlos vive en `printscript-interpreter`, no acá).
- No aplica reglas de formateo (eso es `printscript-formatter`).
- No valida estilo/convenciones (eso es `printscript-linter`).
- No tokeniza el archivo fuente (eso es `printscript-lexer` — este módulo
  recibe `Sequence<Token>` ya armada).