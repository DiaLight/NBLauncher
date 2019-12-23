package dialight.minecraft.json.libs;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Extract {

    @SerializedName("exclude")
    private final List<String> exclude;

    public Extract(List<String> exclude) {
        this.exclude = exclude;
    }

    public List<String> getExclude() {
        return exclude;
    }

}
