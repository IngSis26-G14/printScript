package formatter

import common.model.diagnostic.Diagnostic
import common.model.doc.Doc
import common.model.rule.BooleanRuleValue
import common.model.rule.IntegerRuleValue
import common.model.rule.Rule
import common.model.rule.StringRuleValue
import common.type.outcome.Outcome
import formatter.rule.IfBraceSameLineRule
import formatter.rule.IndentsInsideIfBlockRule
import formatter.rule.LineBreakAfterStatementRule
import formatter.rule.LineBreaksBeforePrintlnRule
import formatter.rule.MandatorySingleSpaceRule
import formatter.rule.NoSpacingAroundEqualsRule
import formatter.rule.SpacingAfterColonRule
import formatter.rule.SpacingAroundEqualsRule
import formatter.rule.SpacingAroundOperatorRule
import formatter.rule.SpacingBeforeColonRule
import lexer.PrintScriptLexer
import parser.PrintScriptParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PrintScriptFormatterTest {
    @Test
    fun `default rules normalize whitespace and always end statements with a newline`() {
        val source = "\tlet  x \n :number=1+2;  println( x );"
        assertEquals("let x: number = 1 + 2;\nprintln(x);\n", format(source))
    }

    @Test
    fun `all spacing options work in either direction regardless of config order`() {
        for (before in listOf(false, true)) {
            for (after in listOf(false, true)) {
                for (equals in listOf(false, true)) {
                    val rules = listOf(
                        Rule(SpacingBeforeColonRule.signature, BooleanRuleValue(before)),
                        Rule(SpacingAfterColonRule.signature, BooleanRuleValue(after)),
                        Rule(SpacingAroundEqualsRule.signature, BooleanRuleValue(equals)),
                    )
                    val b = if (before) " " else ""
                    val a = if (after) " " else ""
                    val e = if (equals) " " else ""
                    val expected = "let x$b:$a" + "number$e=$e" + "1;\nx$e=$e" + "2;\n"
                    val source = "let x : number = 1; x = 2;"
                    assertEquals(expected, format(source, rules))
                    assertEquals(expected, format(source, rules.reversed()))
                }
            }
        }
    }

    @Test
    fun `newlines are inserted before println and final newline is retained`() {
        for (count in 0..2) {
            val rules = listOf(Rule(LineBreaksBeforePrintlnRule.signature, IntegerRuleValue(count)))
            val blankLines = "\n".repeat(count)
            val source = "let x:number=1;println(x);x=2;println(x);"
            val expected = "let x: number = 1;\n" + blankLines + "println(x);\nx = 2;\n" + blankLines + "println(x);\n"
            assertEquals(expected, format(source, rules))
        }
    }

    @Test
    fun `nested blocks use configured indentation and braces stay with if and else`() {
        val source = "const flag:boolean=true; if(flag)\n{let x:number=1;if(flag){println(x+2);}}else\n{println('no');}"
        for (width in listOf(0, 2, 4)) {
            val indent = " ".repeat(width)
            val rules = listOf(Rule(IndentsInsideIfBlockRule.signature, IntegerRuleValue(width)))
            val expected = "const flag: boolean = true;\nif (flag) {\n" +
                indent + "let x: number = 1;\n" + indent + "if (flag) {\n" +
                indent.repeat(2) + "println(x + 2);\n" + indent + "}\n" +
                "} else {\n" + indent + "println('no');\n}\n"
            assertEquals(expected, format(source, rules))
            assertEquals(expected, format(expected, rules))
        }
    }

    @Test
    fun `blank lines and indentation compose inside blocks`() {
        val rules = listOf(
            Rule(IndentsInsideIfBlockRule.signature, IntegerRuleValue(2)),
            Rule(LineBreaksBeforePrintlnRule.signature, IntegerRuleValue(1)),
        )
        assertEquals("if (flag) {\n\n  println(1);\n}\n", format("if(flag){println(1);}", rules))
    }

    @Test
    fun `formatting preserves string contents and stabilizes after one pass`() {
        val source = "let x:string='a  +  b';println(x+' : = ; ');"
        val expected = "let x: string = 'a  +  b';\nprintln(x + ' : = ; ');\n"
        assertEquals(expected, format(source))
        assertEquals(expected, format(expected))
        val unary = format("println(1+-2);println(--3);")
        assertEquals("println(1 + - 2);\nprintln( - - 3);\n", unary)
        assertEquals(unary, format(unary))
    }

    @Test
    fun `empty blocks remain valid and format consistently`() {
        assertEquals("if (flag) {\n} else {\n}\n", format("if(flag){}else{}"))
    }

    @Test
    fun `invalid config and disabling mandatory rules produce diagnostics`() {
        val invalid = listOf(
            Rule(LineBreaksBeforePrintlnRule.signature, IntegerRuleValue(-1)),
            Rule(LineBreaksBeforePrintlnRule.signature, IntegerRuleValue(3)),
            Rule(IndentsInsideIfBlockRule.signature, IntegerRuleValue(-1)),
            Rule(SpacingAfterColonRule.signature, StringRuleValue("yes")),
            Rule("unknown", BooleanRuleValue(true)),
        ) + listOf(MandatorySingleSpaceRule, SpacingAroundOperatorRule, LineBreakAfterStatementRule, IfBraceSameLineRule)
            .map { Rule(it.signature, BooleanRuleValue(false)) }
        for (rule in invalid) {
            val result = PrintScriptFormatter().format("1.1", emptySequence(), listOf(rule)).single()
            assertIs<Outcome.Error<Diagnostic>>(result, rule.toString())
        }
        assertIs<Outcome.Error<Diagnostic>>(
            PrintScriptFormatter().format("1.0", emptySequence(), listOf(Rule(IndentsInsideIfBlockRule.signature, IntegerRuleValue(2)))).single(),
        )
    }

    @Test
    fun `legacy no spacing option works and conflicting options fail`() {
        val noSpacing = Rule(NoSpacingAroundEqualsRule.signature, BooleanRuleValue(true))
        assertEquals("let x: number=1;\n", format("let x:number=1;", listOf(noSpacing)))
        val conflicting = listOf(noSpacing, Rule(SpacingAroundEqualsRule.signature, BooleanRuleValue(true)))
        assertIs<Outcome.Error<Diagnostic>>(PrintScriptFormatter().format("1.1", emptySequence(), conflicting).single())
    }

    private fun format(source: String, rules: List<Rule> = emptyList()): String {
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
