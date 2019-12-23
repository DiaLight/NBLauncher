package dialight.minecraft;

import dialight.minecraft.json.Library;

import java.io.File;

public class ResolvedNative {

    private final File file;
    private final Library library;

    public ResolvedNative(File file, Library library) {
        this.file = file;
        this.library = library;
    }

    public File getFile() {
        return file;
    }

    public Library getLibrary() {
        return library;
    }

}
