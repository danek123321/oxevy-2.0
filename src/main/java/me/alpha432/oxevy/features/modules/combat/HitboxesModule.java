package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.Oxevy;

public class HitboxesModule extends Module {
    private final Setting<Float> expand = num("Expand", 1.5f, 0.1f, 3f);
    private final Setting<Boolean> entities = bool("Entities", true);
    private final Setting<Boolean> players = bool("Players", true);
    private final Setting<Boolean> mobs = bool("Mobs", true);

    public HitboxesModule() {
        super("Hitboxes", "Expands entity hitboxes", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        Oxevy.hitboxesEnabled = true;
        Oxevy.hitboxesExpand = expand.getValue();
        Oxevy.hitboxesEntities = entities.getValue();
        Oxevy.hitboxesPlayers = players.getValue();
        Oxevy.hitboxesMobs = mobs.getValue();
    }

    @Override
    public void onDisable() {
        Oxevy.hitboxesEnabled = false;
    }

    @Override
    public void onTick() {
        Oxevy.hitboxesExpand = expand.getValue();
        Oxevy.hitboxesEntities = entities.getValue();
        Oxevy.hitboxesPlayers = players.getValue();
        Oxevy.hitboxesMobs = mobs.getValue();
    }
}
