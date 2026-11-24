package printscript.idea;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import editor.EditorService;
import org.jetbrains.annotations.NotNull;

public final class FormatAction extends DumbAwareAction {
    @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    @Override public void update(@NotNull AnActionEvent event) {
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        event.getPresentation().setEnabledAndVisible(file != null && file.getFileType() == PrintScriptFileType.INSTANCE
            && file.isWritable() && event.getData(CommonDataKeys.EDITOR) != null);
    }
    @Override public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
        if (project != null && editor != null && file != null) format(project, editor, file);
    }

    static void format(Project project, Editor editor, PsiFile file) {
        var document = editor.getDocument();
        String original = document.getText();
        long stamp = document.getModificationStamp();
        Settings settings = Settings.forFile(file);
        if (settings.error() != null) {
            Messages.showErrorDialog(project, settings.error(), "PrintScript");
            return;
        }
        new Task.Backgroundable(project, "Formatting PrintScript", true) {
            @Override public void run(@NotNull ProgressIndicator indicator) {
                var result = new EditorService(com.intellij.openapi.progress.ProgressManager::checkCanceled).format(original, settings.version(), settings.format());
                indicator.checkCanceled();
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed() || editor.isDisposed() || indicator.isCanceled()) return;
                    if (result.getFormattedText() == null) {
                        String message = result.getIssues().stream().map(issue -> issue.getMessage()).reduce((a, b) -> a + "\n" + b).orElse("Formatting failed");
                        Messages.showErrorDialog(project, message, "PrintScript");
                        return;
                    }
                    if (document.getModificationStamp() != stamp) {
                        Messages.showInfoMessage(project, "The document changed during formatting. Run the action again.", "PrintScript");
                        return;
                    }
                    if (!original.equals(result.getFormattedText())) {
                        WriteCommandAction.writeCommandAction(project, file).withName("Format PrintScript File").run(
                            () -> document.setText(result.getFormattedText()));
                    }
                });
            }
        }.queue();
    }
}
