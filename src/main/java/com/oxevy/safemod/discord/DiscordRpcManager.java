package com.oxevy.safemod.discord;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;

public class DiscordRpcManager {
    private static final Logger LOG = LogManager.getLogger();
    private static boolean enabled = false;
    private static Class<?> rpcClass = null;

    // Attempts to initialize Discord RPC via reflection to avoid hard dependency.
    public static void init(String clientId) {
        if (clientId == null || clientId.isEmpty()) {
            LOG.info("Discord RPC: empty clientId, skipping init.");
            return;
        }

        // Try a few common class names that libraries might expose.
        String[] candidateClasses = new String[] {
            "com.discord.rpc.DiscordRPC",
            "net.dhleong.discordrpc.DiscordRPC",
            "org.javacord.api.DiscordRPC" // (Note: placeholder; real name varies)
        };

        for (String className : candidateClasses) {
            try {
                Class<?> cls = Class.forName(className);
                // Try common init methods
                // 1) initialize(String)
                try {
                    Method m = cls.getMethod("initialize", String.class);
                    m.invoke(null, clientId);
                    enabled = true;
                    rpcClass = cls;
                    LOG.info("Discord RPC initialized using " + className);
                    return;
                } catch (NoSuchMethodException ignored) { /* try next */ }

                // 2) start(String)
                try {
                    Method m = cls.getMethod("start", String.class);
                    m.invoke(null, clientId);
                    enabled = true;
                    rpcClass = cls;
                    LOG.info("Discord RPC initialized using " + className);
                    return;
                } catch (NoSuchMethodException ignored) { /* try next */ }
            } catch (ClassNotFoundException e) {
                // try next candidate
            } catch (Exception e) {
                LOG.error("Discord RPC initialization error with " + className, e);
            }
        }

        if (!enabled) {
            LOG.info("Discord RPC library not found on classpath. Rich Presence disabled.");
        }
    }

    // Update presence if library is loaded
    public static void updatePresence(String details, String state) {
        if (!enabled || rpcClass == null) return;
        try {
            // Try static updatePresence method: updatePresence(String, String)
            try {
                Method m = rpcClass.getMethod("updatePresence", String.class, String.class);
                m.invoke(null, details, state);
                return;
            } catch (NoSuchMethodException ignored) { /* try instance method */ }

            // Try instance-based if library exposes INSTANCE
            try {
                Object instance = rpcClass.getField("INSTANCE").get(null);
                Method m = rpcClass.getMethod("updatePresence", String.class, String.class);
                m.invoke(instance, details, state);
            } catch (Exception ignored) {
                // give up
            }
        } catch (Exception e) {
            LOG.error("Discord RPC: failed to update presence", e);
        }
    }

    // Shutdown RPC cleanly
    public static void shutdown() {
        if (!enabled || rpcClass == null) return;
        try {
            Method m = rpcClass.getMethod("shutdown");
            m.invoke(null);
        } catch (Exception e) {
            LOG.warn("Discord RPC: shutdown failed", e);
        }
    }
}
