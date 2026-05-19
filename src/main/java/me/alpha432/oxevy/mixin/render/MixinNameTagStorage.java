package me.alpha432.oxevy.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.NameTagsModule;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NameTagFeatureRenderer.Storage.class)
public class MixinNameTagStorage {
    @Shadow @Final
    List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsSeethrough;

    @Shadow @Final
    List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsNormal;

    @Inject(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void onAdd(PoseStack matrices, @Nullable Vec3 vec3d, int i, Component text, boolean bl, int j, double d, CameraRenderState state, CallbackInfo ci) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags != null && nameTags.isEnabled()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void wrapLabelScale(PoseStack matrices, float x, float y, float z, Operation<Void> original, PoseStack matrices2, @Nullable Vec3 vec3d, int i, Component text, boolean bl, int j, double d, CameraRenderState state) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags == null || !nameTags.isEnabled()) {
            original.call(matrices, x, y, z);
            return;
        }

        float scale = 0.025F * nameTags.getScale();
        double distance = Math.sqrt(d);
        if (distance > 10)
            scale *= distance / 10;

        original.call(matrices, scale, -scale, scale);
    }

    @ModifyVariable(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), argsOnly = true)
    private boolean forceNotSneaking(boolean notSneaking) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        return nameTags != null && nameTags.isEnabled() || notSneaking;
    }

    @ModifyReceiver(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private List<SubmitNodeStorage.NameTagSubmit> swapFirstList(List<SubmitNodeStorage.NameTagSubmit> originalList, Object labelCommand) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags != null && nameTags.isEnabled() && nameTags.isSeeThrough())
            if (originalList == nameTagSubmitsNormal)
                return nameTagSubmitsSeethrough;
        return originalList;
    }

    @ModifyReceiver(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    private List<SubmitNodeStorage.NameTagSubmit> swapSecondList(List<SubmitNodeStorage.NameTagSubmit> originalList, Object labelCommand) {
        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags != null && nameTags.isEnabled() && nameTags.isSeeThrough())
            if (originalList == nameTagSubmitsSeethrough)
                return nameTagSubmitsNormal;
        return originalList;
    }
}
