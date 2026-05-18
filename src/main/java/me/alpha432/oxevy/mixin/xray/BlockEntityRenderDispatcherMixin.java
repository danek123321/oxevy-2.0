package me.alpha432.oxevy.mixin.xray;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.XRayModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true)
    private <S extends BlockEntityRenderState> void onRenderRenderState(
        S renderState, PoseStack matrices, SubmitNodeCollector queue,
        CameraRenderState cameraRenderState, CallbackInfo ci)
    {
        if (Oxevy.moduleManager == null) return;
        XRayModule xray = Oxevy.moduleManager.getModuleByClass(XRayModule.class);
        if (xray != null && xray.shouldHideBlockEntity(renderState))
            ci.cancel();
    }
}
