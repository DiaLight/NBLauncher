package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.BiConsumer;

public class Artifact {

    @SerializedName("url")
    private final String url;
    @SerializedName("sha1")
    private final String sha1;
    @SerializedName("size")
    private final int size;

    public Artifact() {
        this(null);
    }

    public Artifact(String url) {
        this(url, null);
    }

    public Artifact(String url, String sha1) {
        this(url, sha1, 0);
    }

    public Artifact(String url, String sha1, int size) {
        this.url = url;
        this.sha1 = sha1;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public String getSha1() {
        return sha1;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "url='" + url + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", size=" + size +
                '}';
    }

}
