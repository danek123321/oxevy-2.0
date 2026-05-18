package me.alpha432.oxevy.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.joml.Matrix4f;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.HandCharmsModule;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRenderer {
    
         @Inject(
        method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRenderArmWithItem(AbstractClientPlayer player,
                                     float tickProgress, float pitch, InteractionHand hand,
                                     float swingProgress, ItemStack item, float equipProgress,
                                     PoseStack matrices, SubmitNodeCollector submitNodeCollector,
                                     int light, CallbackInfo ci) {
        
        // Check if hand charms module is enabled and should apply
        HandCharmsModule handCharms = (HandCharmsModule) Oxevy.moduleManager.getModuleByClass(HandCharmsModule.class);
        if (handCharms != null && handCharms.shouldApply()) {
            // Apply our custom transformations
            matrices.translate(
                (float) handCharms.offsetX.getValue(),
                (float) handCharms.offsetY.getValue(),
                (float) handCharms.offsetZ.getValue()
            );
            
            // Apply rotations
            PoseStack.Pose pose = matrices.last();
            Matrix4f matrix = pose.pose();
            matrix.rotate((float) Math.toRadians(handCharms.rotateX.getValue()), 1F, 0F, 0F);
            matrix.rotate((float) Math.toRadians(handCharms.rotateY.getValue()), 0F, 1F, 0F);
            matrix.rotate((float) Math.toRadians(handCharms.rotateZ.getValue()), 0F, 0F, 1F);
            
            // Apply scale
            matrices.scale(
                (float) handCharms.scale.getValue(),
                (float) handCharms.scale.getValue(),
                (float) handCharms.scale.getValue()
            );
        }
    }
}