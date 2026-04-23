package me.alpha432.oxevy.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.Feature;
import me.alpha432.oxevy.features.commands.ModuleCommand;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.modules.client.*;
import me.alpha432.oxevy.features.modules.combat.*;
import me.alpha432.oxevy.features.modules.hud.*;
import me.alpha432.oxevy.features.modules.misc.*;
import me.alpha432.oxevy.features.modules.movement.*;
import me.alpha432.oxevy.features.modules.player.*;
import me.alpha432.oxevy.features.modules.render.*;
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
        // CLIENT
        register(new ClickGuiModule());
        register(new HudEditorModule());
        register(new NotificationsModule());
        register(new ClientSpoof());
        register(new fakeplayer());
        register(new RPCModule());

        // COMBAT
        register(new KillAuraModule());
        register(new AimBotModule());
        register(new CriticalsModule());
        register(new KeyPearlModule());
        register(new Strafe());
        register(new AutoCobwebModule());
        register(new AutoTotemModule());
        register(new AutoTrapModule());
        register(new HitboxesModule());
        register(new ReachModule());

        // HUD
        register(new WatermarkHudModule());
        register(new CoordinatesHudModule());
        register(new FpsHudModule());
        register(new ServerInfoHudModule());
        register(new ArrayListHudModule());
        register(new KeystrokesHudModule());
        register(new PotionEffectsHudModule());
        register(new TargetHudModule());
        register(new TargetInfoHudModule());
        register(new ArmorHudModule());
        register(new MenuWatermark());

        // MISC
        register(new MCFModule());
        register(new AutoToolModule());

        // MOVEMENT
        register(new StepModule());
        register(new ReverseStepModule());
        register(new SpeedHack());
        register(new Flight());
        register(new FreeCam());
        register(new ScaffoldModule());
        register(new TimerModule());
        register(new SprintModule());
        register(new NoSlowModule());
        register(new SpiderModule());
        register(new SafeWalkModule());
        register(new BlinkModule());

        // PLAYER
        register(new FastPlaceModule());
        register(new VelocityModule());
        register(new NoFallModule());
        register(new AirPlaceModule());
        register(new AntiCobwebModule());
        register(new AutoEatModule());
        register(new FastBreakModule());
        register(new NukerModule());
        register(new AutoClickerModule());
        register(new AutoRespawnModule());

        // RENDER
        register(new BlockHighlightModule());
        register(new NametagsModule());
        register(new ChestESPModule());
        register(new TracerModule());
        register(new FullbrightModule());

        // Set default enabled modules
        FpsHudModule fps = (FpsHudModule) getModuleByClass(FpsHudModule.class);
        if (fps != null) {
            fps.enable();
            fps.showMinMax.setValue(false);
            fps.showAverage.setValue(true);
            fps.showFrameTime.setValue(false);
            fps.showGraph.setValue(false);
            fps.performanceWarnings.setValue(false);
        }

        ArrayListHudModule arrayList = (ArrayListHudModule) getModuleByClass(ArrayListHudModule.class);
        if (arrayList != null) {
            arrayList.enable();
        }

        NotificationsModule notifs = (NotificationsModule) getModuleByClass(NotificationsModule.class);
        if (notifs != null) {
            notifs.enable();
        }

        KillAuraModule killAura = (KillAuraModule) getModuleByClass(KillAuraModule.class);
        if (killAura != null) {
            killAura.setBind(org.lwjgl.glfw.GLFW.GLFW_KEY_R);
        }

        FullbrightModule fullbright = (FullbrightModule) getModuleByClass(FullbrightModule.class);
        if (fullbright != null) {
            fullbright.enable();
        }

        LOGGER.info("Registered {} modules", modules.size());

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
