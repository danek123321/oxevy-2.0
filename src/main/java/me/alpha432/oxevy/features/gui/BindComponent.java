package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Bind;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.KeyboardUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BindComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();

    private final Setting<Bind> bindSetting;
    private boolean listening = false;

    public BindComponent(Setting<Bind> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        this.bindSetting = setting;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);

        context.drawString(mc.font, bindSetting.getName(), x + 5, y + (height - 8) / 2, 0xFFBBBBBB);

        String valueText;
        if (listening) {
            valueText = "Press a key...";
        } else {
            int key = bindSetting.getValue() != null ? bindSetting.getValue().getKey() : -1;
            valueText = KeyboardUtil.getKeyName(key);
        }

        int valueColor = listening ? ClickGuiModule.getInstance().color.getValue().getRGB() : 0xFFFFFFFF;
        context.drawString(mc.font, valueText, x + width - 5 - mc.font.width(valueText), y + (height - 8) / 2, valueColor);

        if (isHovered(mouseX, mouseY)) {
            RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;

        if (button == 0) {
            listening = true;
        } else if (button == 1) {
            bindSetting.setValue(Bind.none());
            listening = false;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        // no-op
    }

    public boolean isListening() {
        return listening;
    }

    public void stopListening() {
        listening = false;
    }
}

