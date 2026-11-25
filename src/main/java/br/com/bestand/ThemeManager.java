package br.com.bestand;

import javafx.scene.Scene;

public class ThemeManager {

    private static boolean darkTheme = false;

    public static boolean isDarkTheme() {
        return darkTheme;
    }

    public static void toggleTheme(Scene scene) {
        darkTheme = !darkTheme;
        applyTheme(scene);
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;

        String lightCss = ThemeManager.class.getResource("themes/theme-light.css").toExternalForm();
        String darkCss = ThemeManager.class.getResource("themes/theme-dark.css").toExternalForm();
        String globalCss = ThemeManager.class.getResource("styles/global.css").toExternalForm();

        scene.getStylesheets().clear();
        scene.getStylesheets().add(darkTheme ? darkCss : lightCss);
        scene.getStylesheets().add(globalCss);
    }
}