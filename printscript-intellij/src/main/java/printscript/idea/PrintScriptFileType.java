package printscript.idea;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrintScriptFileType extends LanguageFileType {
    public static final Language LANGUAGE = new Language("PrintScript") {};
    public static final PrintScriptFileType INSTANCE = new PrintScriptFileType();

    private PrintScriptFileType() { super(LANGUAGE); }
    @Override public @NotNull String getName() { return "PrintScript"; }
    @Override public @NotNull String getDescription() { return "PrintScript source"; }
    @Override public @NotNull String getDefaultExtension() { return "ps"; }
    @Override public @Nullable Icon getIcon() { return null; }
}
