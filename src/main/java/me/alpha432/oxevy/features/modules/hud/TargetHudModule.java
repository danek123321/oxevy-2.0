package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class TargetHudModule extends HudModule {
    private Entity target = null;
    private float displayHealth;

    public TargetHudModule() {
        super("TargetHUD", "Shows target info", 160, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;
        Entity newTarget = mc.crosshairPickEntity;
        if (newTarget != null && newTarget.isAlive() && newTarget != mc.player) target = newTarget;
        if (target == null || !target.isAlive() || mc.player.distanceTo(target) > 10) return;

        GuiGraphics ctx = e.getContext();
        float x = getX(); float y = getY();
        int accent = 0xFF7A2FF7;

        RenderUtil.roundRect(ctx, x, y, (float)getWidth(), (float)getHeight(), 12f, 0xAA0D0D0D);
        
        if (target instanceof LivingEntity living) {
            displayHealth = AnimationUtil.animate(displayHealth, living.getHealth(), 0.15f, AnimationUtil.Easing.EASE_OUT);
            ctx.drawString(mc.font, living.getName().getString(), (int)x + 12, (int)y + 10, 0xFFFFFFFF);
            
            float hpPercent = displayHealth / living.getMaxHealth();
            RenderUtil.roundRect(ctx, x + 12, y + 28, (float)getWidth() - 24, 6, 3f, 0x33FFFFFF);
            RenderUtil.roundRect(ctx, x + 12, y + 28, (hpPercent * (getWidth() - 24)), 6, 3f, accent);
            
            if (target instanceof Player player) {
                int armorX = (int)x + 12;
                for (int i = 3; i >= 0; i--) {
                    ItemStack stack = player.getItemBySlot(EquipmentSlot.values()[i + 2]);
                    if (!stack.isEmpty()) { ctx.renderItem(stack, armorX, (int)y + 38); armorX += 20; }
                }
            }
        }
    }
}
