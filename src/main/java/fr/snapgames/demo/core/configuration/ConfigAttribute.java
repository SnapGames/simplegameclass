package fr.snapgames.demo.core.configuration;

import fr.snapgames.demo.core.math.physic.PhysicEngine;
import fr.snapgames.demo.core.math.physic.World;

import java.awt.*;
import java.util.function.Function;

/**
 * This enumeration define all the possible configuration attributes to be used
 * by the {@link Configuration} class.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public enum ConfigAttribute {
    /**
     * Define the Window title configuration attribute.
     */
    TITLE(
            "window title",
            "game.title",
            "title,t",
            "Set the window title",
            "MyTitle",
            v -> v),
    /**
     * Define the debug level configuration attribute.
     */
    DEBUG(
            "debug level",
            "game.debug",
            "debug,d",
            "Set the debug level",
            0,
            Integer::valueOf),
    /**
     * Define the Screen resolution configuration attribute.
     */
    SCREEN_RESOLUTION(
            "screen resolution",
            "game.screen.resolution",
            "resolution,r",
            "define the screen resolution (pixel rated !)",
            new Dimension(320, 200),
            ConfigAttribute::toDimension),
    /**
     * Define the Window size configuration attribute.
     */
    WINDOW_SIZE(
            "window size",
            "game.window.size",
            "size,s",
            "define the window size",
            new Dimension(320, 200),
            ConfigAttribute::toDimension),
    /**
     * Define the play area for the {@link World} object in {@link PhysicEngine}
     * configuration attribute.
     */
    PHYSIC_PLAY_AREA(
            "play area used as world limit",
            "game.physic.play.area",
            "playarea,p",
            "define the play area size",
            new Dimension(320, 200),
            ConfigAttribute::toDimension),
    /**
     * Define the gravity for the {@link World} object in {@link PhysicEngine}
     * configuration attribute.
     */
    PHYSIC_GRAVITY(
            "gravity used for physic world",
            "game.physic.gravity",
            "gravity,g",
            "define the physic gravity to apply to any entity",
            0.981,
            Double::valueOf);

    private final String name;

    private static Dimension toDimension(String value) {
        String[] interpretedValue = value
                .split("x");
        return new Dimension(
                Integer.parseInt(interpretedValue[0]),
                Integer.parseInt(interpretedValue[1]));
    }

    String configAttributeKey;
    String argName;
    Object defaultValue;

    String description;
    Function<String, Object> attrParser;

    ConfigAttribute(String name, String c, String a, String d, Object v, Function<String, Object> p) {
        this.name = name;
        this.attrParser = p;
        this.description = d;
        this.configAttributeKey = c;
        this.argName = a;
        this.defaultValue = v;
    }

    public String getName() {
        return this.name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getConfigAttributeKey() {
        return this.configAttributeKey;
    }

    public String getDescription() {
        return this.description;
    }

    public String getArgName() {
        return this.argName;
    }

    public Function<String, Object> getAttrParser() {
        return this.attrParser;
    }
}
