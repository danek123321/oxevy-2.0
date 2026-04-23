package me.alpha432.oxevy;

import me.alpha432.oxevy.manager.*;
import me.alpha432.oxevy.util.BuildConfig;
import me.alpha432.oxevy.util.TextUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Oxevy implements ModInitializer, ClientModInitializer {
    public static float TIMER = 1f;

    public static boolean hitboxesEnabled = false;
    public static float hitboxesExpand = 1.5f;
    public static boolean hitboxesEntities = true;
    public static boolean hitboxesPlayers = true;
    public static boolean hitboxesMobs = true;

    public static final Logger LOGGER = LogManager.getLogger("Oxevy");
    public static ServerManager serverManager;
    public static ColorManager colorManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static HoleManager holeManager;
    public static EventManager eventManager;
    public static SpeedManager speedManager;
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static ConfigManager configManager;
    public static TargetManager targetManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Pre-initializing {} v{}",
                BuildConfig.NAME, BuildConfig.VERSION);
        configManager = new ConfigManager();
        eventManager = new EventManager();
        serverManager = new ServerManager();
        targetManager = new TargetManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        friendManager = new FriendManager();
        colorManager = new ColorManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        holeManager = new HoleManager();

        TextUtil.init();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}", BuildConfig.NAME);

        long startTime = System.nanoTime();

        eventManager.init();
        commandManager.init();
        moduleManager.init();
        friendManager.init();

        configManager.load();
        colorManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));

        long endTime = System.nanoTime();

        LOGGER.info("Initialized {} in {}ms",
                BuildConfig.NAME, (endTime - startTime) / 1000000.0);
    }
}
