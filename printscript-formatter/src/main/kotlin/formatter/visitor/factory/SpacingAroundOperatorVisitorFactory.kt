package formatter.visitor.factory

import common.model.node.NodeType
import common.model.rule.BooleanRuleValue
import common.model.rule.Rule
import common.model.rule.RuleType
import common.model.visitor.context.ContextVisitor
import formatter.rule.SpacingAroundOperatorRule
import formatter.visitor.SpacingAroundOperatorVisitor

internal class SpacingAroundOperatorVisitorFactory(
    private val operators: Collection<NodeType>,
) : ContextVisitorFactory {
    override val ruleType: RuleType = SpacingAroundOperatorRule

    override fun create(rule: Rule): ContextVisitor {
        val enforce = (rule.value as BooleanRuleValue).value
        return SpacingAroundOperatorVisitor(enforce, operators)
    }
}
