package me.alpha432.oxevy.mixin.entity;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.AntiBlindModule;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F", at = @At("HEAD"), cancellable = true)
    private void onGetEffectFadeFactor(Holder<MobEffect> registryEntry, float delta, CallbackInfoReturnable<Float> cir) {
        if (registryEntry != MobEffects.DARKNESS) return;

        AntiBlindModule antiBlind = Oxevy.moduleManager.getModuleByClass(AntiBlindModule.class);
        if (antiBlind.isEnabled())
            cir.setReturnValue(0F);
    }
}
