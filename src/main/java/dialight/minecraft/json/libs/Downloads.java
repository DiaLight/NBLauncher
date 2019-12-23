package dialight.minecraft.json.libs;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.json.ArtifactWithPath;

import java.util.Collections;
import java.util.Map;

public class Downloads {

    @SerializedName("artifact")
    private final ArtifactWithPath artifact;
    @SerializedName("classifiers")
    private final Map<String, ArtifactWithPath> classifiers;

    public Downloads() {
        this(null);
    }

    public Downloads(ArtifactWithPath artifact) {
        this(artifact, Collections.emptyMap());
    }

    public Downloads(ArtifactWithPath artifact, Map<String, ArtifactWithPath> classifiers) {
        this.artifact = artifact;
        this.classifiers = classifiers;
    }

    public ArtifactWithPath getArtifact() {
        return artifact;
    }

    public Map<String, ArtifactWithPath> getClassifiers() {
        return classifiers;
    }

    @Override
    public String toString() {
        return "Downloads{" +
                "artifact=" + artifact +
                ", classifiers=" + classifiers +
                '}';
    }

}
