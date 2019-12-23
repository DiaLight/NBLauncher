package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class Logging {

    @SerializedName("client")
    private final LoggingClient client;


    public Logging(LoggingClient client) {
        this.client = client;
    }

    public LoggingClient getClient() {
        return client;
    }

}
