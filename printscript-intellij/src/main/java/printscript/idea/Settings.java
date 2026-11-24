package printscript.idea;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;

record Settings(String version, Path lint, Path format, String error) {
    static Settings forFile(PsiFile file) {
        VirtualFile virtual = file.getVirtualFile();
        if (virtual == null) return new Settings("1.1", null, null, null);
        String base = file.getProject().getBasePath();
        VirtualFile directory = virtual.getParent();
        Path lint = null;
        Path format = null;
        while (directory != null) {
            if (lint == null) lint = childPath(directory, "lint.json");
            if (format == null) format = childPath(directory, "format.json");
            VirtualFile config = directory.findChild(".printscript.properties");
            if (config != null) {
                try {
                    var properties = new Properties();
                    properties.load(new StringReader(new String(config.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8)));
                    String version = properties.getProperty("version", "1.1").trim();
                    if (!version.equals("1.0") && !version.equals("1.1")) {
                        return new Settings(version, lint, format, "PrintScript version must be 1.0 or 1.1");
                    }
                    return new Settings(version,
                        resolve(directory, properties.getProperty("lint.config"), lint),
                        resolve(directory, properties.getProperty("format.config"), format), null);
                } catch (IOException | IllegalArgumentException exception) {
                    return new Settings("1.1", lint, format, "Cannot read PrintScript settings: " + exception.getMessage());
                }
            }
            if (directory.getPath().equals(base)) break;
            directory = directory.getParent();
        }
        return new Settings("1.1", lint, format, null);
    }

    private static Path childPath(VirtualFile directory, String name) {
        VirtualFile child = directory.findChild(name);
        return child == null ? null : Path.of(child.getPath());
    }

    private static Path resolve(VirtualFile directory, String value, Path fallback) {
        return value == null ? fallback : Path.of(directory.getPath()).resolve(value.trim()).normalize();
    }
}
