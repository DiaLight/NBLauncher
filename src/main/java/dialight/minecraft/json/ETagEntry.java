package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ETagEntry {

    @SerializedName("url")
    private String url;

    @SerializedName("eTag")
    private String eTag;

    @SerializedName("hash")
    private String hash;

    @SerializedName("local")
    private long local;

    @SerializedName("remote")
    private Date remote;

    public ETagEntry(String url, String eTag, String hash, long local, Date remote) {
        this.url = url;
        this.eTag = eTag;
        this.hash = hash;
        this.local = local;
        this.remote = remote;
    }

    public String getUrl() {
        return url;
    }

    public String geteTag() {
        return eTag;
    }

    public String getHash() {
        return hash;
    }

    public long getLocal() {
        return local;
    }

    public Date getRemote() {
        return remote;
    }
}
