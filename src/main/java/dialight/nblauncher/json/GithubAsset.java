package dialight.nblauncher.json;

import com.google.gson.annotations.SerializedName;

public class GithubAsset {

    @SerializedName("name")
    private String name;

    @SerializedName("size")
    private long size;

    @SerializedName("browser_download_url")
    private String browser_download_url;

    @SerializedName("content_type")
    private String content_type;

    @SerializedName("download_count")
    private int download_count;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getBrowser_download_url() {
        return browser_download_url;
    }

    public String getContent_type() {
        return content_type;
    }

    public int getDownload_count() {
        return download_count;
    }

}
