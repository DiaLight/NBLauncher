package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class ArtifactWithPath extends Artifact {

    @SerializedName("path")
    private final String path;

    public ArtifactWithPath() {
        this(null);
    }

    public ArtifactWithPath(String path) {
        this(path, null);
    }

    public ArtifactWithPath(String path, String url) {
        this(path, url, null);
    }

    public ArtifactWithPath(String path, String url, String sha1) {
        this(path, url, sha1, 0);
    }

    public ArtifactWithPath(String path, String url, String sha1, int size) {
        super(url, sha1, size);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ArtifactWithPath{" +
                "path='" + path + '\'' +
                ", url='" + getUrl() + '\'' +
                ", sha1='" + getSha1() + '\'' +
                ", size=" + getSize() +
                '}';
    }

    public boolean validatePath() {
        if(path.startsWith("/")) return false;
        if(path.contains(":")) return false;
        if(path.contains("..")) return false;
        return true;
    }

}
