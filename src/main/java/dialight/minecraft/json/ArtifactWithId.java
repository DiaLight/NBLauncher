package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class ArtifactWithId extends Artifact {

    @SerializedName("id")
    private final String id;

    public ArtifactWithId() {
        this(null);
    }

    public ArtifactWithId(String id) {
        this(id, null);
    }

    public ArtifactWithId(String id, String url) {
        this(id, url, null);
    }

    public ArtifactWithId(String id, String url, String sha1) {
        this(id, url, sha1, 0);
    }

    public ArtifactWithId(String id, String url, String sha1, int size) {
        super(url, sha1, size);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ArtifactWithId{" +
                "id='" + id + '\'' +
                ", url='" + getUrl() + '\'' +
                ", sha1='" + getSha1() + '\'' +
                ", size=" + getSize() +
                '}';
    }

}
