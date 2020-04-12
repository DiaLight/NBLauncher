package dialight.nblauncher.json;

import com.google.gson.annotations.SerializedName;
import dialight.misc.FileUtils;

import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class GithubRelease {

    @SerializedName("tag_name")
    private String tag_name;

    @SerializedName("prerelease")
    private boolean prerelease;

    @SerializedName("assets")
    private List<GithubAsset> assets;

    public String getTag_name() {
        return tag_name;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public List<GithubAsset> getAssets() {
        return assets;
    }

}
