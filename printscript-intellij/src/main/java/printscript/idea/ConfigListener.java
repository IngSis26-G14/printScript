package printscript.idea;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Recheck open source files after saved configuration changes. */
public final class ConfigListener implements BulkFileListener {
    @Override public void after(@NotNull List<? extends VFileEvent> events) {
        if (events.stream().noneMatch(event -> event.getPath().endsWith(".json") || event.getPath().endsWith(".printscript.properties"))) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            for (var project : ProjectManager.getInstance().getOpenProjects()) {
                if (!project.isDisposed()) DaemonCodeAnalyzer.getInstance(project).restart();
            }
        });
    }
}
