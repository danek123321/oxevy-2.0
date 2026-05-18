package me.alpha432.oxevy.mixin.entity;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.TrueSightModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"), cancellable = true)
    private void onIsInvisibleTo(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;

        TrueSightModule trueSight = Oxevy.moduleManager.getModuleByClass(TrueSightModule.class);
        if (trueSight.isEnabled())
            cir.setReturnValue(false);
    }
}
