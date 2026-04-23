package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.Feature;
import me.alpha432.oxevy.features.gui.items.Item;
import me.alpha432.oxevy.features.gui.items.buttons.ModuleButton;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import me.alpha432.oxevy.util.ColorUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class OxevyGui extends Screen {
    private static OxevyGui INSTANCE;
    private static Color colorClipboard = null;

    static {
        INSTANCE = new OxevyGui();
    }

    private final ArrayList<Widget> widgets = new ArrayList<>();
    
    // Animation values for GUI open/close
    private float guiOpenAnimation = 0.0f;
    private boolean wasOpen = false;

    public OxevyGui() {
        super(Component.literal("Oxevy"));
        setInstance();
        load();
    }

    public static OxevyGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OxevyGui();
        }
        return INSTANCE;
    }

    public static OxevyGui getClickGui() {
        return OxevyGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    private void load() {
        int x = -84;
        for (Module.Category category : Oxevy.moduleManager.getCategories()) {
            if (category == Module.Category.HUD) continue;
            Widget panel = new Widget(category.getName(), x += 90, 4, true);
            Oxevy.moduleManager.stream()
                    .filter(m -> m.getCategory() == category && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            this.widgets.add(panel);
        }
        this.widgets.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    private void applySearchFilter() {
        // Search filter disabled
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        me.alpha432.oxevy.features.gui.items.Item.context = context;

        // Update GUI open animation
        float animSpeed = ClickGuiModule.getInstance().animationsEnabled.getValue() ?
            ClickGuiModule.getInstance().animationSpeed.getValue() : 1.0f;
        guiOpenAnimation = AnimationUtil.animate(guiOpenAnimation, 1.0f, animSpeed, AnimationUtil.Easing.EASE_OUT_BACK);

        // Apply pop-up scale animation
        float scale = ClickGuiModule.getInstance().animationsEnabled.getValue() ?
            0.5f + (guiOpenAnimation * 0.5f) : 1.0f;

        // Apply fade animation
        float alpha = ClickGuiModule.getInstance().animationsEnabled.getValue() ? guiOpenAnimation : 1.0f;

        // Draw semi-transparent background with fade animation
        int bgColor = new Color(0, 0, 0, (int)(120 * alpha)).hashCode();
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), bgColor);

        applySearchFilter();

        // Apply pop-up animation transform
        float centerX = context.guiWidth() / 2f;
        float centerY = context.guiHeight() / 2f;

        context.pose().pushMatrix();
        context.pose().translate(centerX, centerY);
        context.pose().scale(scale, scale);
        context.pose().translate(-centerX, -centerY);

        // Draw widgets with animation
        this.widgets.forEach(components -> {
            components.drawScreen(context, mouseX, mouseY, delta);
        });

        context.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.widgets.forEach(components -> components.mouseClicked((int) click.x(), (int) click.y(), click.button()));
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.widgets.forEach(components -> components.mouseReleased((int) click.x(), (int) click.y(), click.button()));
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount < 0) {
            this.widgets.forEach(component -> component.setY(component.getY() - 10));
        } else if (verticalAmount > 0) {
            this.widgets.forEach(component -> component.setY(component.getY() + 10));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }
    
    @Override
    public boolean charTyped(CharacterEvent input) {
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }//ignore 1.21.8 blur thing

    public final ArrayList<Widget> getComponents() {
        return this.widgets;
    }

    public int getTextOffset() {
        return -6;
    }

    public static Color getColorClipboard() {
        return colorClipboard;
    }

    public static void setColorClipboard(Color color) {
        colorClipboard = color;
    }
}
