package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class BunnyHopModule extends Module {

    public Setting<JumpIf> jumpIf = mode("Jump If", JumpIf.SPRINTING);

    public BunnyHopModule() {
        super("BunnyHop", "Automatically jumps while moving", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.onGround() || mc.player.isShiftKeyDown()) return;

        if (jumpIf.getValue().shouldJump(mc.player))
            mc.player.jumpFromGround();
    }

    @Override
    public String getDisplayInfo() {
        return jumpIf.getValue().name;
    }

    public enum JumpIf {
        SPRINTING("Sprinting"),
        WALKING("Walking"),
        ALWAYS("Always");

        private final String name;

        JumpIf(String name) {
            this.name = name;
        }

        boolean shouldJump(net.minecraft.client.player.LocalPlayer p) {
            return switch (this) {
                case SPRINTING -> p.isSprinting() && (p.zza != 0 || p.xxa != 0);
                case WALKING -> p.zza != 0 || p.xxa != 0;
                case ALWAYS -> true;
            };
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
