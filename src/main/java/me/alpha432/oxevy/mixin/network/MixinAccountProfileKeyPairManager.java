package me.alpha432.oxevy.mixin.network;

import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Silences Mojang keypair retrieval spam when the session is unauthorized (401).
 * This affects chat/profile signing only; gameplay is unaffected.
 */
@Mixin(net.minecraft.client.multiplayer.AccountProfileKeyPairManager.class)
public class MixinAccountProfileKeyPairManager {

    @Redirect(
        method = "fetchProfileKeyPair",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/UserApiService;getKeyPair()Lcom/mojang/authlib/yggdrasil/response/KeyPairResponse;"
        )
    )
    private KeyPairResponse oxevy$swallowUnauthorizedKeyPairFetch(UserApiService service) {
        try {
            return service.getKeyPair();
        } catch (MinecraftClientException e) {
            if (isUnauthorized401(e)) return null;
            throw e;
        }
    }

    private static boolean isUnauthorized401(Throwable t) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            if (cur.toString().contains("Status: 401")) return true;
        }
        return false;
    }
}
