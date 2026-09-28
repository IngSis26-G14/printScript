package formatter

import common.model.diagnostic.Diagnostic
import common.model.doc.Doc
import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.StringRuleValue
import common.type.outcome.Outcome
import formatter.rule.IfBraceBelowLineRule
import formatter.rule.IfBraceSameLineRule
import formatter.rule.IndentsInsideIfBlockRule
import formatter.rule.LineBreakAfterStatementRule
import formatter.rule.LineBreaksAfterPrintlnRule
import formatter.rule.LineBreaksBeforePrintlnRule
import formatter.rule.MandatorySingleSpaceRule
import formatter.rule.NoSpacingAroundEqualsRule
import formatter.rule.SpacingAfterColonRule
import formatter.rule.SpacingAroundEqualsRule
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptFormatterTest {
    @Test
    fun `println line breaks replace existing blank lines and omit a final separator`() {
        val source = "let value:string = 'x';\nprintln(value);\n\n\nprintln('end');"
        for (count in 0..2) {
            val rules = listOf(Rule(LineBreaksAfterPrintlnRule.signature, IntegerRuleValue(count)))
            val expected = "let value:string = 'x';\nprintln(value);" + "\n".repeat(count + 1) + "println('end');"
            assertEquals(expected, format(source, rules))
        }
    }

    @Test
    fun `statement line breaks replace existing spacing and omit a final separator`() {
        val source = "let x:number=1;\nlet y:number=2;let z:number=3;"
        val rules = listOf(Rule(LineBreakAfterStatementRule.signature, BooleanRuleValue(true)))
        assertEquals("let x:number=1;\nlet y:number=2;\nlet z:number=3;", format(source, rules))
    }

    @Test
    fun `configured rules do not apply unrelated formatting`() {
        val source = "let x  :number=1;"
        val rules = listOf(Rule(SpacingAfterColonRule.signature, BooleanRuleValue(true)))

        assertEquals("let x  : number=1;", format(source, rules))
    }

    @Test
    fun `line breaks after println use the TCK rule name and direction`() {
        val source = "println(1);let x:number;"
        val rules = listOf(Rule(LineBreaksAfterPrintlnRule.signature, IntegerRuleValue(2)))

        assertEquals("println(1);\n\n\nlet x:number;", format(source, rules))
    }

    @Test
    fun `brace rules support both layouts`() {
        val source = "if(flag)\n{println(1);}"

        assertEquals(
            "if(flag) {println(1);}",
            format(source, listOf(Rule(IfBraceSameLineRule.signature, BooleanRuleValue(true)))),
        )
        assertEquals(
            "if(flag)\n{println(1);}",
            format(source, listOf(Rule(IfBraceBelowLineRule.signature, BooleanRuleValue(true)))),
        )
    }

    @Test
    fun `invalid configuration produces diagnostics`() {
        val invalid = listOf(
            Rule(LineBreaksAfterPrintlnRule.signature, IntegerRuleValue(-1)),
            Rule(LineBreaksBeforePrintlnRule.signature, IntegerRuleValue(3)),
            Rule(IndentsInsideIfBlockRule.signature, IntegerRuleValue(-1)),
            Rule(SpacingAfterColonRule.signature, StringRuleValue("yes")),
            Rule("unknown", BooleanRuleValue(true)),
            Rule(MandatorySingleSpaceRule.signature, BooleanRuleValue(false)),
            Rule(IfBraceSameLineRule.signature, BooleanRuleValue(false)),
        )
        for (rule in invalid) {
            val result = PrintScriptFormatter().format("1.1", emptySequence(), listOf(rule)).single()
            assertIs<Outcome.Error<Diagnostic>>(result, rule.toString())
        }
    }

    @Test
    fun `legacy no spacing option works and conflicting options fail`() {
        val noSpacing = Rule(NoSpacingAroundEqualsRule.signature, BooleanRuleValue(true))
        assertEquals("let x:number=1;", format("let x:number = 1;", listOf(noSpacing)))

        val conflicting = listOf(noSpacing, Rule(SpacingAroundEqualsRule.signature, BooleanRuleValue(true)))
        assertIs<Outcome.Error<Diagnostic>>(
            PrintScriptFormatter().format("1.1", emptySequence(), conflicting).single(),
        )
    }

    private fun format(source: String, rules: List<Rule>): String {
        val tokens = PrintScriptLexer().lex("1.1", source.asSequence()).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        val nodes = PrintScriptParser().parse("1.1", tokens).map {
            when (it) {
                is Outcome.Ok -> it.value
                is Outcome.Error -> error(it.error.format())
            }
        }
        return PrintScriptFormatter().format("1.1", nodes, rules).joinToString("") {
            assertIs<Outcome.Ok<Doc>>(it).value.format()
        }
    }
}
