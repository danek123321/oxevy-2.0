package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class ArmorHudModule extends HudModule {
    public final Setting<Boolean> showDurability = bool("ShowDurability", true);
    public final Setting<Boolean> showItemNames = bool("ShowItemNames", false);
    public final Setting<Boolean> showArmorPoints = bool("ShowArmorPoints", true);
    public final Setting<Boolean> showToughness = bool("ShowToughness", true);
    public final Setting<Boolean> showItems = bool("ShowItems", true);

    public ArmorHudModule() {
        super("Armor", "Displays player's armor durability and stats", 100, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;

        Player player = mc.player;
        if (player == null) {
            setWidth(80);
            setHeight(mc.font.lineHeight + 4);
            return;
        }

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        // Armor points
        if (showArmorPoints.getValue()) {
            float armor = (float) player.getAttributeValue(Attributes.ARMOR);
            String armorStr = "§7Armor: §f" + String.format(Locale.US, "%.1f", armor);
            ctx.drawString(mc.font, armorStr, (int) x, (int) drawY, 0xFFAAAAAA);
            maxWidth = Math.max(maxWidth, mc.font.width(armorStr));
            drawY += lineHeight;
        }

        // Toughness
        if (showToughness.getValue()) {
            float toughness = (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            String toughnessStr = "§7Toughness: §f" + String.format(Locale.US, "%.1f", toughness);
            ctx.drawString(mc.font, toughnessStr, (int) x, (int) drawY, 0xFFAAAAAA);
            maxWidth = Math.max(maxWidth, mc.font.width(toughnessStr));
            drawY += lineHeight;
        }

        // Armor items
        if (showItems.getValue()) {
            List<EquipmentSlot> armorSlots = List.of(
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
            );

            int iconSize = 16;
            int padding = 2;
            int totalWidth = 0;

            for (EquipmentSlot slot : armorSlots) {
                ItemStack stack = player.getItemBySlot(slot);
                String slotName = switch (slot) {
                    case HEAD -> "Helmet";
                    case CHEST -> "Chestplate";
                    case LEGS -> "Leggings";
                    case FEET -> "Boots";
                    default -> "Unknown";
                };

                // Draw item icon
                ctx.renderItem(stack, (int) x, (int) drawY);
                ctx.renderItemDecorations(mc.font, stack, (int) x, (int) drawY);

                // Draw item name if enabled
                if (showItemNames.getValue() && !stack.isEmpty()) {
                    String itemName = stack.getHoverName().getString();
                    ctx.drawString(mc.font, itemName, (int) x + iconSize + padding, (int) drawY + mc.font.lineHeight - 2, 0xFFAAAAAA);
                    totalWidth = Math.max(totalWidth, iconSize + padding + mc.font.width(itemName));
                } else {
                    // Draw slot name if no item and item names disabled
                    if (stack.isEmpty() && !showItemNames.getValue()) {
                        ctx.drawString(mc.font, slotName, (int) x + iconSize + padding, (int) drawY + mc.font.lineHeight - 2, 0xFF555555);
                        totalWidth = Math.max(totalWidth, iconSize + padding + mc.font.width(slotName));
                    } else if (stack.isEmpty()) {
                        totalWidth = Math.max(totalWidth, iconSize);
                    } else {
                        totalWidth = Math.max(totalWidth, iconSize + padding + mc.font.width(stack.getHoverName().getString()));
                    }
                }

                if (stack.isEmpty()) {
                    // Draw empty slot indicator
                    ctx.fill((int) x, (int) drawY, (int) x + iconSize, (int) drawY + iconSize, 0x33000000);
                }

                drawY += iconSize + padding;
            }

            setWidth(Math.max(80, totalWidth + 6));
            return; // Skip text-based display if showing items
        }

        // Armor durability (text-based)
        if (showDurability.getValue()) {
            List<EquipmentSlot> armorSlots = List.of(
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
            );

            for (EquipmentSlot slot : armorSlots) {
                ItemStack stack = player.getItemBySlot(slot);
                String slotName = switch (slot) {
                    case HEAD -> "Helmet";
                    case CHEST -> "Chestplate";
                    case LEGS -> "Leggings";
                    case FEET -> "Boots";
                    default -> "Unknown";
                };

                if (stack.isEmpty()) {
                    String emptyStr = "§7" + slotName + ": §cNone";
                    ctx.drawString(mc.font, emptyStr, (int) x, (int) drawY, 0xFFAAAAAA);
                    maxWidth = Math.max(maxWidth, mc.font.width(emptyStr));
                } else {
                    int maxDamage = stack.getMaxDamage();
                    int damage = stack.getDamageValue();
                    int durability = maxDamage > 0 ? ((maxDamage - damage) * 100 / maxDamage) : 100;
                    Color color = durability > 50 ? new Color(0, 255, 0) :
                                  durability > 25 ? new Color(255, 255, 0) :
                                                   new Color(255, 0, 0);
                    String durStr = "§7" + slotName + ": §f" + durability + "%";
                    if (showItemNames.getValue() && !stack.isEmpty()) {
                        durStr += " §8(" + stack.getHoverName().getString() + ")";
                    }
                    ctx.drawString(mc.font, durStr, (int) x, (int) drawY, color.getRGB());
                    maxWidth = Math.max(maxWidth, mc.font.width(durStr));
                }
                drawY += lineHeight;
            }
        }

        setWidth(Math.max(80, maxWidth + 6));
        setHeight(drawY - y);
    }
}