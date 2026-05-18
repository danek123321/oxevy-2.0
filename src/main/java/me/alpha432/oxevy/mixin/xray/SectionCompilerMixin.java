package me.alpha432.oxevy.mixin.xray;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.render.XRayModule;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @ModifyVariable(method = "getOrBeginLayer",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0)
    private ChunkSectionLayer forceTranslucentLayer(
        ChunkSectionLayer renderType)
    {
        if (Oxevy.moduleManager == null) return renderType;
        XRayModule xray = Oxevy.moduleManager.getModuleByClass(XRayModule.class);
        if (xray != null && xray.isOpacityMode())
            return ChunkSectionLayer.TRANSLUCENT;

        return renderType;
    }
}
