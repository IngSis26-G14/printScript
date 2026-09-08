package formatter.model.value

import common.model.doc.Doc
import common.model.value.Value

internal data class DocValue(val value: Doc) : Value {
    override val type = DocValueType
    override fun format(): String = value.toString()
}
