package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class NameTagsModule extends Module {
    public final Setting<Float> scale = num("Scale", 1.0f, 0.05f, 5.0f);
    public final Setting<Boolean> unlimitedRange = bool("UnlimitedRange", true);
    public final Setting<Boolean> seeThrough = bool("SeeThrough", false);
    public final Setting<Boolean> forceMobNametags = bool("ForceMobNametags", true);
    public final Setting<Boolean> forcePlayerNametags = bool("ForcePlayerNametags", false);
    public final Setting<Boolean> armor = bool("Armor", true);
    public final Setting<Boolean> ping = bool("Ping", true);
    public final Setting<Boolean> healthBar = bool("HealthBar", true);
    public final Setting<Boolean> playerWatermark = bool("PlayerWatermark", true);

    public NameTagsModule() {
        super("NameTags", "Customizes entity nametags", Category.RENDER);
    }

    public float getScale() {
        return scale.getValue();
    }

    public boolean isUnlimitedRange() {
        return isEnabled() && unlimitedRange.getValue();
    }

    public boolean isSeeThrough() {
        return isEnabled() && seeThrough.getValue();
    }

    public boolean shouldForceMobNametags() {
        return isEnabled() && forceMobNametags.getValue();
    }

    public boolean shouldForcePlayerNametags() {
        return isEnabled() && forcePlayerNametags.getValue();
    }
}
