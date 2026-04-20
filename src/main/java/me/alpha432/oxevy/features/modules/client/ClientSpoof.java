package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import java.lang.reflect.Field;

/**
 * ClientSpoof: Spoof client brand/info with reflection (robust and version-tolerant).
 */
public class ClientSpoof extends Module {
    private final Setting<Mode> mode = mode("Mode", Mode.VANILLA);
    private final Setting<String> customBrand = str("CustomBrand", "vanilla");
    private final Setting<Boolean> hideMods = bool("HideMods", true);
    private final Setting<Boolean> spoofLanguage = bool("SpoofLanguage", true);
    private final Setting<String> language = str("Language", "en_US");
    private final Setting<Integer> viewDistance = num("ViewDistance", 12, 2, 32);
    private final Setting<Boolean> chatColors = bool("ChatColors", true);
    private final Setting<ChatVisibility> chatVisibility = mode("ChatVisibility", ChatVisibility.ENABLED);

    private String originalBrand;

    public ClientSpoof() {
        super("ClientSpoof", "Spoof your client brand and information", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        // Capture current brand via reflection to restore later
        originalBrand = getCurrentBrand();
        applySpoof();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        restoreBrand();
    }

    private void applySpoof() {
        try {
            Field brandField = mc.getClass().getDeclaredField("brand");
            brandField.setAccessible(true);
            brandField.set(mc, getSpoofedBrand());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreBrand() {
        try {
            Field brandField = mc.getClass().getDeclaredField("brand");
            brandField.setAccessible(true);
            brandField.set(mc, originalBrand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSpoofedBrand() {
        switch (mode.getValue()) {
            case VANILLA:
                return "vanilla";
            case FORGE:
                return "forge";
            case FABRIC:
                return "fabric";
            case LUNAR:
                return "lunarclient:v2.15.1";
            case BADLION:
                return "badlion";
            case LABYMOD:
                return "labymod";
            case CUSTOM:
                return customBrand.getValue();
            default:
                return "vanilla";
        }
    }

    private String getCurrentBrand() {
        try {
            Field brandField = mc.getClass().getDeclaredField("brand");
            brandField.setAccessible(true);
            return (String) brandField.get(mc);
        } catch (Exception e) {
            return "unknown";
        }
    }

    public enum Mode {
        VANILLA,
        FORGE,
        FABRIC,
        LUNAR,
        BADLION,
        LABYMOD,
        CUSTOM
    }

    public enum ChatVisibility {
        ENABLED,
        COMMANDS_ONLY,
        HIDDEN
    }
}
