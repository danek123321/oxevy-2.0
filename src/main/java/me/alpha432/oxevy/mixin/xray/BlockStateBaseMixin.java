package me.alpha432.oxevy.mixin.xray;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.XRayModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateBase.class)
public class BlockStateBaseMixin {
    @Inject(
        method = "getShadeBrightness(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F",
        at = @At("RETURN"),
        cancellable = true,
        order = 980)
    private void onGetShadeBrightness(BlockGetter blockGetter,
        BlockPos blockPos, CallbackInfoReturnable<Float> original)
    {
        if (Oxevy.moduleManager == null) return;
        XRayModule xray = Oxevy.moduleManager.getModuleByClass(XRayModule.class);
        if (xray != null && xray.isEnabled())
            original.setReturnValue(1F);
    }
}
