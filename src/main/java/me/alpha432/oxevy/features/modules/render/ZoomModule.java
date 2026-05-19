package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;

public class ZoomModule extends Module {
    public final Setting<Double> zoom = num("Zoom", 6.0, 1.0, 20.0);
    public final Setting<Boolean> smooth = bool("Smooth", true);

    private double preFov;
    private double value;
    private double time;

    public ZoomModule() {
        super("Zoom", "Zooms your view", Category.RENDER);
    }

    @Override
    public void onEnable() {
        preFov = mc.options.fov().get();
        value = zoom.getValue();
        time = 0.001;
    }

    @Override
    public void onDisable() {
        mc.options.fov().set((int) preFov);
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (!smooth.getValue()) {
            time = isEnabled() ? 1 : 0;
        } else {
            if (isEnabled()) time += event.getDelta() * 5;
            else time -= event.getDelta() * 5;

            time = Math.max(0, Math.min(1, time));
        }

        double scaling = getScaling();
        double newFov = preFov / scaling;
        mc.options.fov().set((int) newFov);
    }

    public double getScaling() {
        double delta = time < 0.5 ? 4 * time * time * time : 1 - Math.pow(-2 * time + 2, 3) / 2;
        return AnimationUtil.lerp((float) 1, (float) value, (float) delta);
    }
}
