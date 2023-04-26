package fr.snapgames.demo.core;

import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

class ConfigurationTest {
    @Test
    public void testConfigurationHasDefaultValue() {
        Configuration config = new Configuration("/test-config.properties", new String[]{});
        Assertions.assertEquals("TestWindowTitle", config.get(ConfigAttribute.TITLE));
        Assertions.assertEquals(2, config.get(ConfigAttribute.DEBUG));
        Assertions.assertEquals(0.981, config.get(ConfigAttribute.PHYSIC_GRAVITY));
        Assertions.assertEquals(new Dimension(640, 400), config.get(ConfigAttribute.WINDOW_SIZE));
        Assertions.assertEquals(new Dimension(640, 400), config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
        Assertions.assertEquals(new Dimension(320, 200), config.get(ConfigAttribute.SCREEN_RESOLUTION));
    }

    @Test
    public void testConfigurationTitleCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"t=TestWindowTitleOverloaded"});
        Assertions.assertEquals("TestWindowTitleOverloaded", config.get(ConfigAttribute.TITLE));
    }

    @Test
    public void testConfigurationDebugCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"d=5"});
        Assertions.assertEquals(5, config.get(ConfigAttribute.DEBUG));
    }

    @Test
    public void testConfigurationGravityCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"g=2.0"});
        Assertions.assertEquals(2.0, config.get(ConfigAttribute.PHYSIC_GRAVITY));
    }

    @Test
    public void testConfigurationWindowSizeCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"s=200x200"});
        Assertions.assertEquals(new Dimension(200, 200), config.get(ConfigAttribute.WINDOW_SIZE));
    }

    @Test
    public void testConfigurationScreenReslutionCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"r=200x200"});
        Assertions.assertEquals(new Dimension(200, 200), config.get(ConfigAttribute.SCREEN_RESOLUTION));
    }

    @Test
    public void testConfigurationPlayAreaCanBeOverloadedFromCLI() {
        Configuration config = new Configuration("/test-config.properties", new String[]{"p=200x200"});
        Assertions.assertEquals(new Dimension(200, 200), config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
    }

}