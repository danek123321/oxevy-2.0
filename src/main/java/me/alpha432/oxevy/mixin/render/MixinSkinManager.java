package me.alpha432.oxevy.mixin.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import me.alpha432.oxevy.Oxevy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(SkinManager.class)
public abstract class MixinSkinManager {

    @Unique
    private static final Map<String, String> capeUrls = new HashMap<>();

    @Unique
    private static boolean capesLoaded = false;

    @Unique
    private MinecraftProfileTexture currentCape;

    @Inject(
        method = "registerTextures(Ljava/util/UUID;Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD")
    )
    private void onRegisterTexturesHead(UUID uuid, com.mojang.authlib.minecraft.MinecraftProfileTextures textures, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        if (!capesLoaded) {
            loadCapes();
        }

        String uuidStr = uuid.toString();
        String capeUrl = capeUrls.get(uuidStr);
        if (capeUrl != null) {
            currentCape = new MinecraftProfileTexture(capeUrl, null);
        } else {
            currentCape = null;
        }
    }

    @ModifyVariable(
        method = "registerTextures(Ljava/util/UUID;Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("STORE"),
        ordinal = 1
    )
    private MinecraftProfileTexture modifyCapeTexture(MinecraftProfileTexture old) {
        if (currentCape == null) return old;
        MinecraftProfileTexture result = currentCape;
        currentCape = null;
        return result;
    }

    @Unique
    private static final String CAPE_URL = "https://raw.githubusercontent.com/danek123321/oxevy-assets/main/Cape.png";

    @Unique
    private static void loadCapes() {
        capesLoaded = true;
        capeUrls.clear();

        try {
            UUID uuid = Minecraft.getInstance().getGameProfile().id();
            capeUrls.put(uuid.toString(), CAPE_URL);
        } catch (Exception e) {
            Oxevy.LOGGER.error("Failed to load capes", e);
        }
    }
}
