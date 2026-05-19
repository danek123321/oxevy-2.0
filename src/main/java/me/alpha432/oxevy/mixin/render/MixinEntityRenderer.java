package me.alpha432.oxevy.mixin.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.HealthTagsModule;
import me.alpha432.oxevy.features.modules.render.NameTagsModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("TAIL"))
    private void addHealthToDisplayName(T entity, S state, float tickProgress, CallbackInfo ci) {
        if (state.nameTag == null) return;
        if (!(entity instanceof LivingEntity le)) return;

        HealthTagsModule healthTags = Oxevy.moduleManager.getModuleByClass(HealthTagsModule.class);
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);

        if (healthTags.isEnabled()) {
            int health = (int) le.getHealth();
            MutableComponent formattedHealth = Component.literal(" ")
                .append(Integer.toString(health))
                .withStyle(health <= 5 ? ChatFormatting.DARK_RED :
                           health <= 10 ? ChatFormatting.GOLD :
                           health <= 15 ? ChatFormatting.YELLOW :
                           ChatFormatting.GREEN);
            state.nameTag = state.nameTag.copy().append(formattedHealth);
        }

        if (nameTags == null || nameTags.isEnabled()) return;

        if (nameTags.healthBar.getValue()) {
            float maxHealth = le.getMaxHealth();
            float health = le.getHealth();
            float ratio = health / maxHealth;
            int segments = 10;
            int filled = Math.round(ratio * segments);

            StringBuilder sb = new StringBuilder(" ");
            for (int i = 0; i < segments; i++) {
                sb.append(i < filled ? '█' : '░');
            }

            MutableComponent bar = Component.literal(sb.toString())
                .withStyle(ratio <= 0.25 ? ChatFormatting.RED :
                           ratio <= 0.5 ? ChatFormatting.GOLD :
                           ratio <= 0.75 ? ChatFormatting.YELLOW :
                           ChatFormatting.GREEN);
            state.nameTag = state.nameTag.copy().append(bar);
        }

        if (entity instanceof Player player && nameTags.armor.getValue()) {
            int armor = player.getArmorValue();
            MutableComponent armorComp = Component.literal(" [" + armor + "⚔]")
                .withStyle(armor >= 15 ? ChatFormatting.GREEN :
                           armor >= 8 ? ChatFormatting.YELLOW :
                           ChatFormatting.RED);
            state.nameTag = state.nameTag.copy().append(armorComp);
        }

        if (entity instanceof Player player && nameTags.watermark.getValue()
            && !player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            MutableComponent mark = Component.literal("O ").withStyle(ChatFormatting.DARK_GREEN);
            state.nameTag = Component.literal("").append(mark).append(state.nameTag);
        }

        if (entity instanceof Player player && nameTags.ping.getValue()) {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(player.getUUID());
                if (info != null) {
                    int latency = info.getLatency();
                    MutableComponent pingComp = Component.literal(" " + latency + "ms")
                        .withStyle(latency <= 50 ? ChatFormatting.GREEN :
                                   latency <= 100 ? ChatFormatting.YELLOW :
                                   latency <= 200 ? ChatFormatting.GOLD :
                                   ChatFormatting.RED);
                    state.nameTag = state.nameTag.copy().append(pingComp);
                }
            }
        }
    }
}
