package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RPCModule extends Module {
    private static final Logger LOG = LoggerFactory.getLogger("RPC");
    private static final String CLIENT_ID = "";

    private static RPCModule instance;
    private static Object rpcInstance;
    private static Method updateMethod;
    private static Method shutdownMethod;

    public RPCModule() {
        super("RPC", "Discord Rich Presence", Category.CLIENT);
        instance = this;
    }

    @Override
    public void onEnable() {
        if (CLIENT_ID.isEmpty()) {
            LOG.info("RPC: No client ID configured");
            disable();
            return;
        }
        initRPC();
    }

    @Override
    public void onDisable() {
        shutdownRPC();
    }

    private void initRPC() {
        try {
            Class<?> clazz = Class.forName("com.discord.rpc.DiscordRPC");
            Class<?> discordRichPresence = Class.forName("com.discord.rpc.DiscordRichPresence");
            Class<?> discordEventHandlers = Class.forName("com.discord.rpc.DiscordEventHandlers");

            Method initMethod = clazz.getMethod("Discord_Initialize", String.class, discordEventHandlers, int.class, String.class);
            updateMethod = clazz.getMethod("Discord_UpdatePresence", discordRichPresence);

            Object handlers = discordEventHandlers.getDeclaredConstructor().newInstance();
            initMethod.invoke(null, CLIENT_ID, handlers, 1, "1.0.0");

            updatePresence("In Game", "Oxevy " + BuildConfig.VERSION, "");
            LOG.info("Discord RPC initialized");
        } catch (Exception e) {
            LOG.warn("Discord RPC not available: " + e.getMessage());
        }
    }

    private void updatePresence(String state, String details, String largeImageKey) {
        if (updateMethod == null) return;
        try {
            Class<?> clazz = Class.forName("com.discord.rpc.DiscordRPC");
            Class<?> discordRichPresence = Class.forName("com.discord.rpc.DiscordRichPresence");

            Object presence = discordRichPresence.getDeclaredConstructor().newInstance();
            discordRichPresence.getField("state").set(presence, state);
            discordRichPresence.getField("details").set(presence, details);
            discordRichPresence.getField("largeImageKey").set(presence, largeImageKey);

            updateMethod.invoke(null, presence);
        } catch (Exception ignored) {}
    }

    private void shutdownRPC() {
        if (shutdownMethod != null) {
            try {
                shutdownMethod.invoke(null);
            } catch (Exception ignored) {}
        }
        LOG.info("Discord RPC shutdown");
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || nullCheck()) return;

        String server = "Singleplayer";
        if (mc.getCurrentServer() != null) {
            server = mc.getCurrentServer().ip;
        }

        String details = server + " | " + mc.player.getName().getString();
        updatePresence("Playing Minecraft", details, "");
    }
}