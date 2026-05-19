package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class TrailModule extends Module {

    public Setting<TrailParticle> particle = mode("Particle", TrailParticle.Flame);
    public Setting<Boolean> pauseWhenStationary = bool("PauseWhenStationary", true);

    public TrailModule() {
        super("Trail", "Renders a trail of particles behind your player", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (pauseWhenStationary.getValue()
            && mc.player.getX() == mc.player.xo
            && mc.player.getY() == mc.player.yo
            && mc.player.getZ() == mc.player.zo) return;

        ParticleOptions type = particle.getValue().getType();
        mc.level.addParticle(type, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0, 0, 0);
    }

    public enum TrailParticle {
        Flame(ParticleTypes.FLAME),
        Heart(ParticleTypes.HEART),
        SoulFlame(ParticleTypes.SOUL_FIRE_FLAME),
        CampfireSmoke(ParticleTypes.CAMPFIRE_COSY_SMOKE),
        Lava(ParticleTypes.LAVA),
        Portal(ParticleTypes.PORTAL),
        EndRod(ParticleTypes.END_ROD),
        Totem(ParticleTypes.TOTEM_OF_UNDYING),
        Note(ParticleTypes.NOTE),
        ElectricSpark(ParticleTypes.ELECTRIC_SPARK),
        GlowSquidInk(ParticleTypes.GLOW_SQUID_INK),
        Cherry(ParticleTypes.CHERRY_LEAVES);

        private final ParticleOptions type;

        TrailParticle(ParticleOptions type) {
            this.type = type;
        }

        public ParticleOptions getType() {
            return type;
        }
    }
}
