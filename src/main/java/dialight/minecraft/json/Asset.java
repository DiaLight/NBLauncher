package dialight.minecraft.json;

import com.google.gson.annotations.SerializedName;

public class Asset {

    @SerializedName("hash")
    private final String hash;
    @SerializedName("size")
    private final int size;

    public Asset(String hash, int size) {
        this.hash = hash;
        this.size = size;
    }

    public String getHash() {
        return hash;
    }

    public int getSize() {
        return size;
    }

}
