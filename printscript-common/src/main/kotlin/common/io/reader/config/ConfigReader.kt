package common.io.reader.config

import common.model.diagnostic.Diagnostic
import common.model.rule.Rule
import common.type.outcome.Outcome

interface ConfigReader {
    fun read(): Outcome<Collection<Rule>, Diagnostic>
}
