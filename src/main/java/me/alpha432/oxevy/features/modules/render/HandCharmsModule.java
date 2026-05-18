package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.traits.Jsonable;
import me.alpha432.oxevy.util.traits.Toggleable;
import me.alpha432.oxevy.Oxevy;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

/**
 * Hand Charms module for customizing hand/viewmodel position
 * Similar to No One's View Model mod
 */
public class HandCharmsModule extends Module implements Jsonable, Toggleable {
    
    // Position settings
    public final Setting<Float> offsetX = num("Offset X", 0.0f, -5.0f, 5.0f);
    public final Setting<Float> offsetY = num("Offset Y", 0.0f, -5.0f, 5.0f);
    public final Setting<Float> offsetZ = num("Offset Z", 0.0f, -5.0f, 5.0f);
    
    // Rotation settings
    public final Setting<Float> rotateX = num("Rotate X", 0.0f, -180.0f, 180.0f);
    public final Setting<Float> rotateY = num("Rotate Y", 0.0f, -180.0f, 180.0f);
    public final Setting<Float> rotateZ = num("Rotate Z", 0.0f, -180.0f, 180.0f);
    
    // Scale settings
    public final Setting<Float> scale = num("Scale", 1.0f, 0.1f, 3.0f);
    
    // Feature toggles
    public final Setting<Boolean> enabled = bool("Enabled", true);
    public final Setting<Boolean> onlyWhenSprinting = bool("Only when sprinting", false);
    public final Setting<Boolean> onlyWhenBlocking = bool("Only when blocking", false);
    
    // Preset modes
    public final Setting<String> preset = new Setting<>("Preset", "NONE");
    
    public HandCharmsModule() {
        super("Hand Charms", "Customize hand/viewmodel position and rotation", Category.RENDER);
        register(enabled);
        register(preset);
        register(offsetX);
        register(offsetY);
        register(offsetZ);
        register(rotateX);
        register(rotateY);
        register(rotateZ);
        register(scale);
        register(onlyWhenSprinting);
        register(onlyWhenBlocking);
    }
    
    public void onEnable() {
        // Apply preset if selected
        String presetValue = preset.getValue();
        if (!presetValue.equals("NONE")) {
            applyPreset(presetValue);
        }
        enabled.setValue(true);
    }
    
    public void onDisable() {
        // Reset to default values
        resetValues();
        enabled.setValue(false);
    }

    public void onTick() {
        // Handle conditional enabling
        if (onlyWhenSprinting.getValue()) {
            if (mc.player != null && !mc.player.isSprinting()) {
                if (enabled.getValue()) {
                    toggle();
                }
            }
        } else if (onlyWhenBlocking.getValue()) {
            if (mc.player != null && !mc.player.isUsingItem()) {
                if (enabled.getValue()) {
                    toggle();
                }
            }
        } else if (!onlyWhenSprinting.getValue() && !onlyWhenBlocking.getValue()) {
            if (!enabled.getValue()) {
                toggle();
            }
        }
    }

    
    
    private void applyPreset(String presetName) {
        switch (presetName.toUpperCase()) {
            case "LOWPOLY":
                offsetX.setValue(0.3f);
                offsetY.setValue(-0.2f);
                offsetZ.setValue(-0.6f);
                rotateX.setValue(15f);
                rotateY.setValue(0f);
                rotateZ.setValue(0f);
                scale.setValue(0.8f);
                break;
            case "CINEMATIC":
                offsetX.setValue(0f);
                offsetY.setValue(-0.4f);
                offsetZ.setValue(-1.2f);
                rotateX.setValue(25f);
                rotateY.setValue(0f);
                rotateZ.setValue(0f);
                scale.setValue(1.2f);
                break;
            case "MINIMAL":
                offsetX.setValue(0f);
                offsetY.setValue(-0.1f);
                offsetZ.setValue(-0.2f);
                rotateX.setValue(0f);
                rotateY.setValue(0f);
                rotateZ.setValue(0f);
                scale.setValue(0.5f);
                break;
            case "DYNAMIC":
                // Dynamic values would be calculated based on movement
                // For now, set some animated values
                double time = System.currentTimeMillis() * 0.001;
                offsetX.setValue((float)(0.1 * Math.sin(time)));
                offsetY.setValue((float)(-0.2 + 0.1 * Math.cos(time * 0.5)));
                offsetZ.setValue(-0.6f);
                rotateX.setValue((float)(10 * Math.sin(time * 0.3)));
                rotateY.setValue(0f);
                rotateZ.setValue((float)(5 * Math.cos(time * 0.2)));
                scale.setValue((float)(0.9 + 0.1 * Math.sin(time * 0.4)));
                break;
            default:
                break;
        }
    }
    
    private void resetValues() {
        offsetX.setValue(0f);
        offsetY.setValue(0f);
        offsetZ.setValue(0f);
        rotateX.setValue(0f);
        rotateY.setValue(0f);
        rotateZ.setValue(0f);
        scale.setValue(1f);
    }
    
    public boolean shouldApply() {
        if (!enabled.getValue()) return false;
        
        if (mc.player == null) return false;
        
        boolean sprinting = mc.player.isSprinting();
        boolean usingItem = mc.player.isUsingItem();
        
        return (!onlyWhenSprinting.getValue() || sprinting) &&
               (!onlyWhenBlocking.getValue() || usingItem);
    }
    
    // Jsonable implementation
    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", enabled.getValue());
        obj.addProperty("offsetX", offsetX.getValue());
        obj.addProperty("offsetY", offsetY.getValue());
        obj.addProperty("offsetZ", offsetZ.getValue());
        obj.addProperty("rotateX", rotateX.getValue());
        obj.addProperty("rotateY", rotateY.getValue());
        obj.addProperty("rotateZ", rotateZ.getValue());
        obj.addProperty("scale", scale.getValue());
        obj.addProperty("onlyWhenSprinting", onlyWhenSprinting.getValue());
        obj.addProperty("onlyWhenBlocking", onlyWhenBlocking.getValue());
        obj.addProperty("preset", preset.getValue());
        return obj;
    }
    
    @Override
    public void fromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) return;
        JsonObject json = element.getAsJsonObject();
        if (json.has("enabled")) enabled.setValue(json.get("enabled").getAsBoolean());
        if (json.has("offsetX")) offsetX.setValue(json.get("offsetX").getAsFloat());
        if (json.has("offsetY")) offsetY.setValue(json.get("offsetY").getAsFloat());
        if (json.has("offsetZ")) offsetZ.setValue(json.get("offsetZ").getAsFloat());
        if (json.has("rotateX")) rotateX.setValue(json.get("rotateX").getAsFloat());
        if (json.has("rotateY")) rotateY.setValue(json.get("rotateY").getAsFloat());
        if (json.has("rotateZ")) rotateZ.setValue(json.get("rotateZ").getAsFloat());
        if (json.has("scale")) scale.setValue(json.get("scale").getAsFloat());
        if (json.has("onlyWhenSprinting")) onlyWhenSprinting.setValue(json.get("onlyWhenSprinting").getAsBoolean());
        if (json.has("onlyWhenBlocking")) onlyWhenBlocking.setValue(json.get("onlyWhenBlocking").getAsBoolean());
        if (json.has("preset")) preset.setValue(json.get("preset").getAsString());
    }
}