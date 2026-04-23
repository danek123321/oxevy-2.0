package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.event.impl.ClientEvent;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.hud.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationsModule extends HudModule {
    public final Setting<Boolean> moduleToggle = bool("Module Toggle", true);
    public final Setting<Integer> duration = num("Duration", 3000, 1000, 10000);
    public final Setting<Integer> maxNotifications = num("Max", 5, 1, 10);

    private static final List<NotificationEntry> notifications = new ArrayList<>();

    public NotificationsModule() {
        super("Notifications", "Displays notifications on screen", 200, 100);
    }

    @Override
    public float getX() {
        return mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() - 210f : 100f;
    }

    @Override
    public float getY() {
        return 10f;
    }

    @Subscribe
    public void onClient(ClientEvent event) {
        if (!moduleToggle.getValue()
                || event.getType() != ClientEvent.Type.TOGGLE_MODULE
                || event.getFeature() instanceof ClickGuiModule) {
            return;
        }

        boolean moduleState = event.getFeature().isEnabled();
        String text = event.getFeature().getName() + " " + (moduleState ? "ON" : "OFF");
        int color = moduleState ? 0xFF55FF55 : 0xFFFF5555;

        addNotification(text, color);
    }

    public static void addNotification(String text, int color) {
        notifications.add(new NotificationEntry(text, color, System.currentTimeMillis()));
        while (notifications.size() > 10) {
            notifications.remove(0);
        }
    }

    @Override
    public void drawContent(Render2DEvent e) {
        long now = System.currentTimeMillis();
        long maxDuration = duration.getValue();

        Iterator<NotificationEntry> it = notifications.iterator();
        float y = getY();
        int lineHeight = mc.font.lineHeight + 2;

        while (it.hasNext()) {
            NotificationEntry entry = it.next();
            long age = now - entry.time;

            if (age > maxDuration) {
                it.remove();
                continue;
            }

            float alpha = 1.0f - ((float) age / maxDuration);
            int bgColor = new Color(0, 0, 0, (int) (150 * alpha)).getRGB();
            int textColor = (entry.color & 0x00FFFFFF) | ((int) (alpha * 255) << 24);

            int width = mc.font.width(entry.text);
            int height = lineHeight;

            GuiGraphics ctx = e.getContext();
            ctx.fill((int) getX(), (int) y, (int) getX() + width + 8, (int) y + height, bgColor);
            ctx.fill((int) getX(), (int) y, (int) getX() + 2, (int) y + height, entry.color);

            ctx.drawString(mc.font, entry.text, (int) getX() + 4, (int) (y + 2), textColor);

            y += lineHeight;
        }

        setWidth(200);
        setHeight(Math.max(50, (int) (y - getY())));
    }

    private static class NotificationEntry {
        String text;
        int color;
        long time;

        NotificationEntry(String text, int color, long time) {
            this.text = text;
            this.color = color;
            this.time = time;
        }
    }
}