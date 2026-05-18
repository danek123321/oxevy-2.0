package me.alpha432.oxevy.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.NameTagsModule;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRendererNameTags<T extends Entity, S extends EntityRenderState> {
    @WrapOperation(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    private double fakeSquaredDistanceToCamera(EntityRenderDispatcher dispatcher, Entity entity, Operation<Double> original, @Share("actualDistanceSq") LocalDoubleRef actualDistanceSq) {
        actualDistanceSq.set(original.call(dispatcher, entity));

        NameTagsModule nameTags = Oxevy.moduleManager.getModuleByClass(NameTagsModule.class);
        if (nameTags != null && nameTags.isUnlimitedRange())
            return 0;

        return actualDistanceSq.get();
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("TAIL"))
    private void restoreSquaredDistanceToCamera(T entity, S state, float tickDelta, CallbackInfo ci, @Share("actualDistanceSq") LocalDoubleRef actualDistanceSq) {
        state.distanceToCameraSq = actualDistanceSq.get();
    }
}
