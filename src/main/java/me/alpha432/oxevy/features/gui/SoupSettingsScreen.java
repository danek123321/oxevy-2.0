package me.alpha432.oxevy.features.gui;

/**
 * Compatibility wrapper (old name). Prefer {@link OxevySettingsScreen}.
 */
public class SoupSettingsScreen extends OxevySettingsScreen {
    public SoupSettingsScreen() {
        super();
    }

    public static void open() {
        OxevySettingsScreen.open();
    }
}

