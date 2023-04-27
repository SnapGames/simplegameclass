package fr.snapgames.demo.core.io.resource;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.system.GameSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource management with a cache principle, avoiding accessing disk when not
 * necessary.
 * It is loadnig-and-caching {@link Font} or {@link BufferedImage} with
 * dedicated getter.
 * access to cached resourceSystem is insered through the file path (String).
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class ResourceManager extends GameSystem {

    public static final String NAME = "ResourceManager";

    private Map<String, Object> resources;

    public ResourceManager(Game g) {
        super(g, NAME);
        resources = new ConcurrentHashMap<>();
    }

    /**
     * Retrieve an image from resourceSystem cache. if not already exist, load it
     * into
     * cache
     *
     * @param file the file to be loaded as an Image
     * @return the corresponding BufferedImage instance.
     */
    public BufferedImage getImage(String file) {
        BufferedImage img = null;
        if (!resources.containsKey(file)) {
            try {
                img = ImageIO.read(Game.class.getResourceAsStream(file));
                resources.putIfAbsent(file, img);
                System.out.printf("INFO: '%s' added as a image resource%n", file);
            } catch (Exception e) {
                System.err.printf("ERROR: Unable to read the image %s%n", file);
            }
        }
        return (BufferedImage) resources.get(file);
    }

    /**
     * Retrieve a font from resourceSystem cache. if not already exist, load it into
     * cache
     *
     * @param file the file to be loaded as an Image
     * @return the corresponding BufferedImage instance.
     */
    public Font getFont(String file) {
        Font font = null;
        if (!resources.containsKey(file)) {
            loadFont(file);
        }
        return (Font) resources.get(file);
    }

    private void loadFont(String path) {
        // load a Font resource
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, ResourceManager.class.getResourceAsStream(path));
            if (font != null) {
                resources.putIfAbsent(path, font);
                System.out.printf("INFO: '%s' added as a font resource%n", path);
            }
        } catch (FontFormatException | IOException e) {
            System.err.printf("ERROR: Unable to read font from %s%n", path);
        }
    }
}
