package printscript.idea;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import editor.*;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class PrintScriptAnnotator extends ExternalAnnotator<PrintScriptAnnotator.Snapshot, EditorResult> implements DumbAware {
    record Snapshot(String text, Settings settings) {}

    @Override public Snapshot collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new Snapshot(editor.getDocument().getText(), Settings.forFile(file));
    }

    @Override public EditorResult doAnnotate(Snapshot snapshot) {
        Settings settings = snapshot.settings();
        if (settings.error() != null) {
            return new EditorResult(snapshot.text(), List.of(new EditorIssue(0, 0, settings.error(), true, false)), null);
        }
        return new EditorService(com.intellij.openapi.progress.ProgressManager::checkCanceled).analyze(snapshot.text(), settings.version(), settings.lint(), settings.format());
    }

    @Override public void apply(@NotNull PsiFile file, EditorResult result, @NotNull AnnotationHolder holder) {
        if (!file.getText().equals(result.getSource())) return;
        int length = file.getTextLength();
        for (EditorIssue issue : result.getIssues()) {
            int start = Math.min(issue.getStart(), length);
            int end = Math.min(issue.getEnd(), length);
            // Give insertions and EOF diagnostics a visible anchor.
            if (end == start && length > 0) {
                start = Math.min(start, length - 1);
                end = start + 1;
            }
            var annotation = holder.newAnnotation(issue.getError() ? HighlightSeverity.ERROR : HighlightSeverity.WARNING, issue.getMessage())
                .range(new TextRange(start, end));
            if (issue.getFormatting()) annotation.withFix(new FormatFix());
            annotation.create();
        }
    }

    private static final class FormatFix implements IntentionAction {
        @Override public @NotNull String getText() { return "Format PrintScript file"; }
        @Override public @NotNull String getFamilyName() { return "PrintScript formatting"; }
        @Override public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return editor != null && file != null && file.getFileType() == PrintScriptFileType.INSTANCE && file.isWritable();
        }
        @Override public void invoke(@NotNull Project project, Editor editor, PsiFile file) { FormatAction.format(project, editor, file); }
        @Override public boolean startInWriteAction() { return false; }
    }
}
