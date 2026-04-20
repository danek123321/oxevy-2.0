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
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue().toLowerCase();
        boolean hasSearch = !searchQuery.isEmpty() && ClickGuiModule.getInstance().searchBarEnabled.getValue();
        
        for (Widget widget : this.widgets) {
            boolean hasVisibleItems = false;
            for (Item item : widget.getItems()) {
                if (item instanceof ModuleButton button) {
                    boolean matches = !hasSearch || button.getModule().getName().toLowerCase().contains(searchQuery);
                    item.setHidden(!matches);
                    if (matches) hasVisibleItems = true;
                }
            }
            // Hide widget if no items match the search
            widget.setHidden(hasSearch && !hasVisibleItems);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Item.context = context;
        
        // Update GUI open animation
        float animSpeed = ClickGuiModule.getInstance().animationsEnabled.getValue() ? 
            ClickGuiModule.getInstance().animationSpeed.getValue() : 1.0f;
        guiOpenAnimation = AnimationUtil.animate(guiOpenAnimation, 1.0f, animSpeed, AnimationUtil.Easing.EASE_OUT_BACK);
        
        // Apply fade animation
        float alpha = ClickGuiModule.getInstance().animationsEnabled.getValue() ? guiOpenAnimation : 1.0f;
        
        // Draw semi-transparent background with fade animation
        int bgColor = new Color(0, 0, 0, (int)(120 * alpha)).hashCode();
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), bgColor);
        
        // Draw search bar and toggle button (always show the button)
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
        int searchWidth = 200;
        int searchX = context.guiWidth() / 2 - searchWidth / 2;
        int searchY = 15;
        int btnWidth = 20;
        int btnHeight = 14;
        int btnX = searchX + searchWidth + 4;
        
        // Search bar background (only if enabled) with fade animation
        if (ClickGuiModule.getInstance().searchBarEnabled.getValue()) {
            int topColorAlpha = (int) (ClickGuiModule.getInstance().topColor.getValue().getAlpha() * alpha);
            int animatedTopColor = (topColorAlpha << 24) | (ClickGuiModule.getInstance().topColor.getValue().getRGB() & 0x00FFFFFF);
            context.fill(searchX - 2, searchY - 2, searchX + searchWidth + 2, searchY + 14, animatedTopColor);
            context.fill(searchX, searchY, searchX + searchWidth, searchY + 10, (int)(0xFF * alpha) << 24);
            
            // Search text with fade animation
            String displayText = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
            // Text color synced to client color
            int guiColor = ColorUtil.toRGBA(ClickGuiModule.getInstance().color.getValue());
            int textColor = searchQuery.isEmpty() ? ColorUtil.toRGBA(new Color(ClickGuiModule.getInstance().color.getValue().getRed(), ClickGuiModule.getInstance().color.getValue().getGreen(), ClickGuiModule.getInstance().color.getValue().getBlue(), Math.max(100, ClickGuiModule.getInstance().color.getValue().getAlpha()))) : guiColor;
            int textWidth = minecraft.font.width(displayText);
            if (textWidth > searchWidth - 4) {
                displayText = minecraft.font.plainSubstrByWidth(displayText, searchWidth - 4) + "...";
            }
            int textColorAnimated = (int)((textColor & 0xFF000000) * alpha) | (textColor & 0x00FFFFFF);
            context.drawString(minecraft.font, displayText, searchX + 4, searchY + 1, textColorAnimated, false);
        }
        
        applySearchFilter();
        
        // Toggle button (always visible) with hover animation
        boolean isHovered = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= searchY && mouseY <= searchY + btnHeight;
        float btnHoverAnim = isHovered ? 1.0f : 0.0f;
        btnHoverAnim = AnimationUtil.animate(btnHoverAnim, btnHoverAnim, 0.15f, AnimationUtil.Easing.EASE_OUT);
        
        int baseBtnColor = ClickGuiModule.getInstance().searchBarEnabled.getValue() ? 0xFF00AA00 : 0xFFAA0000;
        int hoverBtnColor = ClickGuiModule.getInstance().searchBarEnabled.getValue() ? 0xFF00FF00 : 0xFFFF0000;
        int btnColor = AnimationUtil.interpolateColor(baseBtnColor, hoverBtnColor, btnHoverAnim);
        
        context.fill(btnX, searchY, btnX + btnWidth, searchY + btnHeight, btnColor);
        String btnText = ClickGuiModule.getInstance().searchBarEnabled.getValue() ? "ON" : "OFF";
        int textW = minecraft.font.width(btnText);
        context.drawString(minecraft.font, btnText, btnX + (btnWidth - textW) / 2, searchY + 2, 0xFFFFFFFF, false);
        
        // Draw widgets with animation
        this.widgets.forEach(components -> {
            components.drawScreen(context, mouseX, mouseY, delta);
        });
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        // Handle toggle button click (always active)
        if (click.button() == 0) {
            int searchWidth = 200;
            int searchX = minecraft.getWindow().getGuiScaledWidth() / 2 - searchWidth / 2;
            int searchY = 15;
            int btnWidth = 20;
            int btnHeight = 14;
            int btnX = searchX + searchWidth + 4;
            
            if (click.x() >= btnX && click.x() <= btnX + btnWidth && 
                click.y() >= searchY && click.y() <= searchY + btnHeight) {
                ClickGuiModule.getInstance().searchBarEnabled.setValue(
                    !ClickGuiModule.getInstance().searchBarEnabled.getValue());
                return true;
            }
        }
        
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
        boolean searchEnabled = ClickGuiModule.getInstance().searchBarEnabled.getValue();
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
        
        // Handle search-related keys only if search is enabled
        if (searchEnabled) {
            // Handle backspace (key code 259 is backspace)
            if (input.key() == 259 && !searchQuery.isEmpty()) {
                ClickGuiModule.getInstance().searchBar.setValue(searchQuery.substring(0, searchQuery.length() - 1));
                return true;
            }
            
            // Handle escape to clear search (key code 256 is escape)
            if (input.key() == 256) {
                ClickGuiModule.getInstance().searchBar.setValue("");
                return true;
            }
        }
        
        this.widgets.forEach(component -> component.onKeyPressed(input.input()));
        return super.keyPressed(input);
    }
    
    @Override
    public boolean charTyped(CharacterEvent input) {
        boolean searchEnabled = ClickGuiModule.getInstance().searchBarEnabled.getValue();
        String searchQuery = ClickGuiModule.getInstance().searchBar.getValue();
        String character = input.codepointAsString();
        
        // Only add to search if search is enabled and it's a printable character
        if (searchEnabled && !character.isEmpty() && searchQuery.length() < 50) {
            ClickGuiModule.getInstance().searchBar.setValue(searchQuery + character);
            return true;
        }
        
        this.widgets.forEach(component -> component.onKeyTyped(input.codepointAsString(), input.modifiers()));
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
