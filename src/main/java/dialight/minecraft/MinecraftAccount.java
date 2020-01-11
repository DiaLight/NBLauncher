package dialight.minecraft;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.UUID;

public class MinecraftAccount {

    @SerializedName("name")
    private final String name;
    @SerializedName("id")
    private final UUID uuid;
    @SerializedName("clientToken")
    private final String clientToken;
    @SerializedName("accessToken")
    private final String accessToken;

    public MinecraftAccount(String name, UUID uuid, String clientToken, String accessToken) {
        this.name = name;
        this.uuid = uuid;
        this.clientToken = clientToken;
        this.accessToken = accessToken;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getClientToken() {
        return clientToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return "MinecraftProfile{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftAccount that = (MinecraftAccount) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
