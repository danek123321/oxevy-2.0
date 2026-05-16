package me.alpha432.oxevy.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.RenderBlockOutlineEvent;
import me.alpha432.oxevy.features.modules.render.AntiBlindModule;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.alpha432.oxevy.util.traits.Util.EVENT_BUS;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    public void renderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean bl, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (EVENT_BUS.post(new RenderBlockOutlineEvent())) {
            ci.cancel();
        }
    }

    @Inject(method = "doesMobEffectBlockSky", at = @At("HEAD"), cancellable = true)
    private void onDoesMobEffectBlockSky(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        AntiBlindModule antiBlind = Oxevy.moduleManager.getModuleByClass(AntiBlindModule.class);
        if (antiBlind.isEnabled())
            cir.setReturnValue(false);
    }
}
