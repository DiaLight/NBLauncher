package dialight.minecraft.json.versions;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DisplayEntry {

    @SerializedName("id")
    private String id;

    @SerializedName("displayName")
    private String displayName;


    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

}
