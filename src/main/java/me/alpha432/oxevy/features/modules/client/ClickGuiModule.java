package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.ClientEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.gui.OxevyGui;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Bind;
import me.alpha432.oxevy.features.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ClickGuiModule extends Module {
    private static ClickGuiModule INSTANCE;

    public Setting<String> prefix = str("Prefix", ".");
    public Setting<Color> color = color("Color", 0, 0, 255, 180);
    public Setting<ColorPreset> colorPreset = mode("ColorPreset", ColorPreset.CUSTOM);
    public Setting<Color> topColor = color("TopColor", 0, 0, 150, 240);
    public Setting<Boolean> rainbow = bool("Rainbow", false);
    public Setting<Integer> rainbowHue = num("Delay", 240, 0, 600);
    public Setting<Float> rainbowBrightness = num("Brightness", 150.0f, 1.0f, 255.0f);
    public Setting<Float> rainbowSaturation = num("Saturation", 150.0f, 1.0f, 255.0f);
    public Setting<Float> nameTagOffset = num("NameTagOffset", 1.2f, 0.5f, 3.0f);
    public Setting<Float> healthBarOffset = num("HealthBarOffset", 1.0f, 0.5f, 3.0f);
    public Setting<Boolean> searchBarEnabled = bool("SearchBarEnabled", true);
    public Setting<String> searchBar = str("SearchBar", "");
    public Setting<Bind> searchToggleBind = key("SearchToggleBind", new Bind(GLFW.GLFW_KEY_F));
    
    // Animation settings
    public Setting<Boolean> animationsEnabled = bool("AnimationsEnabled", true);
    public Setting<Float> animationSpeed = num("AnimationSpeed", 0.15f, 0.05f, 0.5f);
    public Setting<Boolean> slideInAnimation = bool("SlideInAnimation", true);
    public Setting<Boolean> scaleAnimation = bool("ScaleAnimation", true);

    public ClickGuiModule() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        rainbowHue.setVisibility(v -> rainbow.getValue());
        rainbowBrightness.setVisibility(v -> rainbow.getValue());
        rainbowSaturation.setVisibility(v -> rainbow.getValue());
        searchBar.setVisibility(v -> searchBarEnabled.getValue());
        searchToggleBind.setVisibility(v -> searchBarEnabled.getValue());
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getType() == ClientEvent.Type.SETTING_UPDATE && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                Oxevy.commandManager.setCommandPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to {global} %s", Oxevy.commandManager.getCommandPrefix());
            }
            if (event.getSetting().equals(this.color)) {
                Oxevy.colorManager.setColor(this.color.getPlannedValue());
            }
            if (event.getSetting().equals(this.colorPreset)) {
                ColorPreset preset = this.colorPreset.getPlannedValue();
                if (preset != ColorPreset.CUSTOM) {
                    Oxevy.colorManager.setTargetColor(preset.getColor());
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            return;
        }
        mc.setScreen(OxevyGui.getClickGui());
    }

    @Override
    public void onLoad() {
        Oxevy.colorManager.setColor(this.color.getValue());
        Oxevy.commandManager.setCommandPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGuiModule.mc.screen instanceof OxevyGui)) {
            this.disable();
            return;
        }
        
        // Handle search toggle bind (Ctrl + F)
        boolean ctrlPressed = GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == 1 || 
                              GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == 1;
        if (ctrlPressed && searchToggleBind.getValue().isDown()) {
            searchBarEnabled.setValue(!searchBarEnabled.getValue());
        }
    }

    public static ClickGuiModule getInstance() {
        return INSTANCE;
    }

    /** Color presets for ESP and ClickGUI with smooth transitions. */
    public enum ColorPreset {
        CUSTOM(null),
        BLUE(new Color(0, 120, 255, 180)),
        RED(new Color(255, 60, 60, 180)),
        GREEN(new Color(60, 255, 120, 180)),
        PURPLE(new Color(180, 0, 255, 180)),
        CYAN(new Color(0, 255, 220, 180)),
        ORANGE(new Color(255, 140, 0, 180));

        private final Color color;

        ColorPreset(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color != null ? color : new Color(0, 0, 255, 180);
        }
    }
}