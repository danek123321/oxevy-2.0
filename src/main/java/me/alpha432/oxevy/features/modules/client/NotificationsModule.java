package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.event.impl.ClientEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.TextUtil;
import me.alpha432.oxevy.util.player.ChatUtil;

public class NotificationsModule extends Module {
    private static final String MODULE_FORMAT = "Toggled %s %s %s";

    public Setting<Boolean> moduleToggle = bool("Module Toggle", true);

    public NotificationsModule() {
        super("Notifications", "Displays notifications for various client events", Category.MISC);
    }

    @Subscribe
    public void onClient(ClientEvent event) {
        if (!moduleToggle.getValue()
                || event.getType() != ClientEvent.Type.TOGGLE_MODULE
                || event.getFeature() instanceof ClickGuiModule) {
            return;
        }

        boolean moduleState = event.getFeature().isEnabled();
        ChatUtil.sendMessage(TextUtil.text(MODULE_FORMAT,
                event.getFeature().getName(),
                moduleState ? "{green}" : "{red}",
                moduleState ? "on" : "off"));
    }
}
