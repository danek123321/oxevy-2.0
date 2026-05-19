package me.alpha432.oxevy.mixin.entity;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.NameTagsModule;
import net.minecraft.client.renderer.entity.MobRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobRenderer.class)
public abstract class MixinMobRenderer {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Mob;D)Z", at = @At("HEAD"), cancellable = true)
    private void onHasLabel(CallbackInfoReturnable<Boolean> cir) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags != null && nameTags.isEnabled() && nameTags.mobs.getValue()) {
            cir.setReturnValue(false);
        }
    }
}
