package dialight.minecraft;

import dialight.minecraft.json.Library;

import java.io.File;
import java.nio.file.Path;

public class ResolvedNative {

    private final Path file;
    private final Library library;

    public ResolvedNative(Path file, Library library) {
        this.file = file;
        this.library = library;
    }

    public Path getFile() {
        return file;
    }

    public Library getLibrary() {
        return library;
    }

}
