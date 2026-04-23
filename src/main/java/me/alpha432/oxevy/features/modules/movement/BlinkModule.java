package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;

import java.util.ArrayDeque;
import java.util.Deque;

public class BlinkModule extends Module {
    public final Setting<Integer> pulseDelay = num("PulseDelay", 0, 0, 500);
    public final Setting<Boolean> visualPulse = bool("VisualPulse", true);

    private final Deque<ServerboundSwingPacket> packetQueue = new ArrayDeque<>();

    private long lastPulse = 0;

    public BlinkModule() {
        super("Blink", "Stores and releases packets", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        packetQueue.clear();
        lastPulse = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        sendPackets();
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (pulseDelay.getValue() > 0) {
            if (System.currentTimeMillis() - lastPulse > pulseDelay.getValue()) {
                sendPackets();
                lastPulse = System.currentTimeMillis();
            }
        }
    }

    public void addPacket(ServerboundSwingPacket packet) {
        if (isEnabled()) {
            packetQueue.add(packet);
        }
    }

    private void sendPackets() {
        if (mc.getConnection() == null) return;

        while (!packetQueue.isEmpty()) {
            mc.getConnection().send(packetQueue.poll());
        }
    }

    public int getQueueSize() {
        return packetQueue.size();
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(getQueueSize());
    }
}