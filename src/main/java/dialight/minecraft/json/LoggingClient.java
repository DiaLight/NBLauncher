package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class LoggingClient {

    @SerializedName("argument")
    private final String argument;
    @SerializedName("type")
    private final String type;
    @SerializedName("file")
    private final ArtifactWithId file;

    public LoggingClient(String argument, String type, ArtifactWithId file) {
        this.argument = argument;
        this.type = type;
        this.file = file;
    }

    public String getArgument() {
        return argument;
    }

    public String getType() {
        return type;
    }

    public ArtifactWithId getFile() {
        return file;
    }

}
