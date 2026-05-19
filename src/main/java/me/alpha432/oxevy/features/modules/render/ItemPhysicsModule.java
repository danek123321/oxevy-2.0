package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class ItemPhysicsModule extends Module {

    public Setting<Boolean> randomRotation = bool("RandomRotation", true);
    public Setting<Boolean> bobbing = bool("Bobbing", true);
    public Setting<Float> bobbingSpeed = num("BobbingSpeed", 0.5f, 0.1f, 3.0f);

    public ItemPhysicsModule() {
        super("ItemPhysics", "Applies physics-like transformations to dropped items", Category.RENDER);
    }
}
