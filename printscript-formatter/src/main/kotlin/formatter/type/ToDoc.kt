package formatter.type

import common.model.doc.Doc
import common.model.node.Node

internal fun Node.Composite.toDoc(): Doc {
    return Doc(
        text = format(),
        span = span,
        leading = emptyList(),
        trailing = emptyList(),
    )
}

internal fun Node.Leaf.toDoc(): Doc {
    return Doc(
        text = format(),
        span = span,
        leading = leading,
        trailing = trailing,
    )
}

internal fun Node.toDoc(): Doc = when (this) {
    is Node.Leaf -> toDoc()
    is Node.Composite -> toDoc()
}
