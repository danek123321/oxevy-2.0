package me.alpha432.oxevy.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Silences Mojang keypair retrieval spam when the session is unauthorized (401).
 * This affects chat/profile signing only; gameplay is unaffected.
 */
@Mixin(net.minecraft.client.multiplayer.AccountProfileKeyPairManager.class)
public class MixinAccountProfileKeyPairManager {

    @Inject(method = "fetchProfileKeyPair", at = @At("RETURN"), cancellable = true)
    private void oxevy$swallowUnauthorizedKeyPairFetch(CallbackInfoReturnable<CompletableFuture<Optional<?>>> cir) {
        CompletableFuture<Optional<?>> original = cir.getReturnValue();
        if (original == null) return;

        cir.setReturnValue(original.exceptionally(t -> {
            Throwable root = unwrap(t);
            if (isUnauthorized401(root)) {
                return Optional.empty();
            }
            throw new CompletionException(root);
        }));
    }

    private static Throwable unwrap(Throwable t) {
        Throwable cur = t;
        while (cur instanceof CompletionException && cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur;
    }

    private static boolean isUnauthorized401(Throwable t) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            String s = cur.toString();
            if (s != null && s.contains("Status: 401")) {
                return true;
            }
        }
        return false;
    }
}

