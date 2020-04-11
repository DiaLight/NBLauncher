package dialight.nblauncher.json;

import com.google.gson.annotations.SerializedName;
import dialight.minecraft.MinecraftAccount;

import java.util.List;
import java.util.UUID;

public class AccountsState {

    @SerializedName("profiles")
    private final List<MinecraftAccount> profiles;
    @SerializedName("selected")
    private final UUID selected;

    public AccountsState(List<MinecraftAccount> profiles, UUID selected) {
        this.profiles = profiles;
        this.selected = selected;
    }

    public List<MinecraftAccount> getProfiles() {
        return profiles;
    }

    public UUID getSelected() {
        return selected;
    }

}
