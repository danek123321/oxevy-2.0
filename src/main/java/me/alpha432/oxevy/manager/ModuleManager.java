package me.alpha432.oxevy.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.Feature;
import me.alpha432.oxevy.features.commands.ModuleCommand;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.modules.client.PingSpoof;
import me.alpha432.oxevy.features.modules.client.ClientSpoof;
import me.alpha432.oxevy.features.modules.client.HudEditorModule;
import me.alpha432.oxevy.features.modules.client.NotificationsModule;
import me.alpha432.oxevy.features.modules.combat.AimBotModule;
import me.alpha432.oxevy.features.modules.combat.CriticalsModule;
import me.alpha432.oxevy.features.modules.combat.KeyPearlModule;
import me.alpha432.oxevy.features.modules.combat.KillAuraModule;
import me.alpha432.oxevy.features.modules.hud.ArrayListHudModule;
import me.alpha432.oxevy.features.modules.hud.CoordinatesHudModule;
import me.alpha432.oxevy.features.modules.hud.FpsHudModule;
import me.alpha432.oxevy.features.modules.hud.ServerInfoHudModule;
import me.alpha432.oxevy.features.modules.hud.TargetHudModule;
import me.alpha432.oxevy.features.modules.hud.WatermarkHudModule;
import me.alpha432.oxevy.features.modules.misc.MCFModule;
import me.alpha432.oxevy.features.modules.combat.Strafe;
import me.alpha432.oxevy.features.modules.movement.Flight;
import me.alpha432.oxevy.features.modules.movement.ReverseStepModule;
import me.alpha432.oxevy.features.modules.movement.SpeedBuff;
import me.alpha432.oxevy.features.modules.movement.StepModule;
import me.alpha432.oxevy.features.modules.player.FastPlaceModule;
import me.alpha432.oxevy.features.modules.player.NoFallModule;
import me.alpha432.oxevy.features.modules.player.VelocityModule;
import me.alpha432.oxevy.features.modules.render.BlockHighlightModule;
import me.alpha432.oxevy.features.modules.render.ChestESPModule;
import me.alpha432.oxevy.features.modules.render.TracerModule;
import me.alpha432.oxevy.features.modules.render.ESP;
import me.alpha432.oxevy.features.modules.render.HealthBarModule;
import me.alpha432.oxevy.features.modules.render.NametagsModule;
// TracerModule removed to avoid cross-package conflicts; Autotool module is used instead
import me.alpha432.oxevy.features.modules.misc.AutoToolModule;
import me.alpha432.oxevy.util.traits.Jsonable;
import me.alpha432.oxevy.util.traits.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class ModuleManager implements Jsonable, Util {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModuleManager");

    private final Map<Class<? extends Module>, Module> fastRegistry = new HashMap<>();
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(new WatermarkHudModule());
        register(new CoordinatesHudModule());
        register(new FpsHudModule());
        register(new ServerInfoHudModule());
        register(new ArrayListHudModule());
        register(new TargetHudModule());
        register(new HudEditorModule());
        register(new ClickGuiModule());
        register(new NotificationsModule());
        register(new KillAuraModule());
        register(new AimBotModule());
        register(new CriticalsModule());
        register(new MCFModule());
        register(new StepModule());
        register(new ReverseStepModule());
        register(new SpeedBuff());
        register(new Flight());
        register(new Strafe());
        register(new FastPlaceModule());
        register(new VelocityModule());
        register(new BlockHighlightModule());
        register(new NametagsModule());
        register(new HealthBarModule());
        register(new ChestESPModule());
        register(new TracerModule());
        // Expose client spoofing toggles in the ClickGui as first-class modules
        register(new PingSpoof());
        register(new ClientSpoof());
        // Keybind HUD feature disabled on revert
        register(new ESP());
        register(new AutoToolModule());
        register(new NoFallModule());
        register(new KeyPearlModule());

        LOGGER.info("Registered {} modules", modules.size());

        // Create a command for each module for modules to be configurable via command line
        for (Module module : modules) {
            Oxevy.commandManager.register(new ModuleCommand(module));
        }

        Oxevy.configManager.addConfig(this);
    }

    public void register(Module module) {
        getModules().add(module);
        fastRegistry.put(module.getClass(), module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public Stream<Module> stream() {
        return getModules().stream();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        return (T) fastRegistry.get(clazz);
    }

    public Module getModuleByName(String name) {
        return stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Module getModuleByDisplayName(String display) {
        return stream().filter(m -> m.getDisplayName().equalsIgnoreCase(display)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        return stream().filter(m -> m.getCategory() == category).toList();
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        getModules().forEach(Module::onLoad);
    }

    public void onTick() {
        stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void onUnload() {
        getModules().forEach(EVENT_BUS::unregister);
        getModules().forEach(Module::onUnload);
    }

    public void onKeyPressed(int key) {
        if (key <= 0 || mc.screen != null) return;
        stream().filter(module -> module.getBind().getKey() == key).forEach(Module::toggle);
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Module module : getModules()) {
            object.add(module.getName(), module.toJson());
        }
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        for (Module module : getModules()) {
            module.fromJson(element.getAsJsonObject().get(module.getName()));
        }
    }

    @Override
    public String getFileName() {
        return "modules.json";
    }
}
