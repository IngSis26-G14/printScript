package printscript.idea;

import com.intellij.lang.annotation.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import editor.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Exercises the real SDK adapter APIs without starting a user's IDE. */
public final class PluginSmokeTest {
    public static void main(String[] args) {
        var lexer = new PrintScriptParserDefinition.SourceLexer();
        String text = "let bad_name: number =  6;\n";
        lexer.start(text, 0, text.length(), 0);
        assert lexer.getTokenType() != null;
        assert lexer.getTokenEnd() == text.length();
        lexer.advance();
        assert lexer.getTokenType() == null;
        lexer.start("", 0, 0, 0);
        assert lexer.getTokenType() == null;

        // Cancellation is supplied by IntelliJ in production; this standalone process has no application.
        EditorResult result = new EditorService().analyze(text, "1.0", null, null);
        var annotator = new PrintScriptAnnotator();
        assert result.getIssues().size() == 2;
        var ranges = new ArrayList<TextRange>();
        var fixes = new ArrayList<Object>();
        var severities = new ArrayList<Object>();
        AnnotationBuilder builder = proxy(AnnotationBuilder.class, (object, method, parameters) -> {
            if (method.getName().equals("range")) ranges.add((TextRange) parameters[0]);
            if (method.getName().equals("withFix")) fixes.add(parameters[0]);
            return method.getReturnType() == AnnotationBuilder.class ? object : null;
        });
        AnnotationHolder holder = proxy(AnnotationHolder.class, (object, method, parameters) -> {
            if (method.getName().equals("newAnnotation")) {
                severities.add(parameters[0]);
                return builder;
            }
            throw new AssertionError(method.getName());
        });
        annotator.apply(file(text), result, holder);
        assert ranges.size() == 2;
        assert ranges.get(0).substring(text).equals("bad_name");
        assert ranges.get(1).substring(text).equals("  ");
        assert fixes.size() == 1;
        assert severities.stream().allMatch(s -> s == HighlightSeverity.WARNING);
        annotator.apply(file(text + " "), result, holder);
        assert ranges.size() == 2 : "Stale analysis must not be applied";
        annotator.apply(file("x"), new EditorResult("x", List.of(new EditorIssue(1, 1, "EOF", true, false)), null), holder);
        assert ranges.get(2).equals(new TextRange(0, 1));
        assert severities.get(2) == HighlightSeverity.ERROR;
        System.out.println("Plugin SDK smoke tests passed: lexer, ranges, severity, quick fix, stale results, EOF.");
    }

    private static PsiFile file(String text) {
        return proxy(PsiFile.class, (object, method, parameters) -> switch (method.getName()) {
            case "getText" -> text;
            case "getTextLength" -> text.length();
            default -> throw new AssertionError(method.getName());
        });
    }

    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
