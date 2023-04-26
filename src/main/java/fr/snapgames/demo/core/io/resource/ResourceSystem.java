package fr.snapgames.demo.core.io.resource;

import fr.snapgames.demo.core.Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
public class ResourceSystem {

    Map<String, Object> resources;

    public ResourceSystem() {
        resources = new ConcurrentHashMap<>();
    }

    /**
     * Retrieve an image from resourceSystem cache. if not already exist, load it into
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
            } catch (Exception e) {
                System.err.printf("Unable to read the image %s", file);
            }
            resources.putIfAbsent(file, img);
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
            try {
                font = getFont(file);

            } catch (Exception e) {
                System.err.printf("ERROR: unable to find font file %s: %s", file, e.getMessage());
            }
            resources.putIfAbsent(file, font);
        }
        return (Font) resources.get(file);
    }
}
