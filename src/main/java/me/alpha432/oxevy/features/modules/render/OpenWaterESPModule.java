package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class OpenWaterESPModule extends Module {

    public Setting<Color> openColor = color("Open Water Color", new Color(0, 255, 0, 128));
    public Setting<Color> shallowColor = color("Shallow Water Color", new Color(255, 0, 0, 128));
    public Setting<Float> lineWidth = num("Line Width", 1.5f, 0.5f, 4f);
    public Setting<Boolean> throughWalls = bool("Through Walls", false);

    public OpenWaterESPModule() {
        super("OpenWaterESP", "Shows if your fishing bobber is in open water", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null) return;

        FishingHook bobber = mc.player.fishing;
        if (bobber == null) return;

        AABB box = new AABB(-2, -1, -2, 3, 2, 3).move(bobber.blockPosition());
        boolean open = bobber.calculateOpenWater(bobber.blockPosition());
        Color c = open ? openColor.getValue() : shallowColor.getValue();

        RenderUtil.drawBox(event.getMatrix(), box, c, lineWidth.getValue(), throughWalls.getValue());
    }
}
