package me.alpha432.oxevy.network;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OxevyUserManager {
    private static final Set<UUID> oxevyUsers = new HashSet<>();

    public static void addOxevyUser(UUID uuid) {
        oxevyUsers.add(uuid);
    }

    public static void removeOxevyUser(UUID uuid) {
        oxevyUsers.remove(uuid);
    }

    public static boolean isOxevyUser(net.minecraft.world.entity.player.Player player) {
        return player != null && oxevyUsers.contains(player.getUUID());
    }

    public static boolean isOxevyUser(UUID uuid) {
        return oxevyUsers.contains(uuid);
    }

    public static void clear() {
        oxevyUsers.clear();
    }

    public static void sendHandshake() {
        OxevyPayload.sendHandshake();
    }

    public static void onPlayerDisconnect(UUID uuid) {
        removeOxevyUser(uuid);
    }

    public static void onWorldJoin() {
        clear();
        sendHandshake();
    }
}
