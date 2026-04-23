package com.oxevy.safemod;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        // Initialize Discord Rich Presence if a client ID is provided via env var
        String clientId = System.getenv("OXEVY_DISCORD_CLIENT_ID");
        if (clientId != null && !clientId.isEmpty()) {
            com.oxevy.safemod.discord.DiscordRpcManager.init(clientId);
            LOGGER.info("OxEvY: Discord RPC init attempted with clientId=" + clientId);
        } else {
            LOGGER.info("OxEvY: No Discord RPC client ID provided. Skipping Discord RPC initialization.");
        }
    }
}
