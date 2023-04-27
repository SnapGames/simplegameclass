package fr.snapgames.demo.core.gfx.animation;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.io.resource.ResourceManager;
import fr.snapgames.demo.core.system.GameSystem;
import fr.snapgames.demo.core.system.SystemManager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The {@link Animations} class is a utility to load a bunch of animation
 * defined into a properties file
 * and store in a cache corresponding {@link Animation} instances.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public class Animations extends GameSystem {
    public static final String NAME = "Animations";
    Map<String, Animation> animations = new HashMap<>();

    /**
     * Initialize a bunch of animation fom the animationFile properties.
     *
     * @param animationFile the properties file defining all the animations with
     *                      their frames and times.
     * @see Animations#loadFromFile(String)
     */
    public Animations(Game game, String pathToAnimationFile) {
        super(game, NAME);
        loadFromFile(pathToAnimationFile);
    }

    /**
     * Load animation's frames from animations.properties file to create
     * {@link Animation} object.
     * format of each animation in the properties file
     *
     * <pre>
     * animation_code=[path-to-image-file];[loop/noloop];{[x],[y],[w],[h],[time]+}
     * </pre>
     * <p>
     * Each line in the file will contain one animation. the 3 part of the
     * description of an animation if composed of multiple
     * <code>[x],[y],[w],[h],[time]+</code> section, each defining one frame.
     * <ul>
     * <li><code>path-to-image-file</code> path to the image file to extract frames
     * from</li>
     * <li><code>loop/noloop</code> loop => the animation will loop.</li>
     * <li><code>x</code> horizontal position in the image file of this frame</li>
     * <li><code>y</code> vertical position in the image file of this frame</li>
     * <li><code>w</code> width of the frame in the image file</li>
     * <li><code>h</code> height of the frame in the image file</li>
     * <li><code>time</code> time duration for this frame in the animation</li>
     * </ul>
     *
     * @param animationFile
     */
    private void loadFromFile(String animationFile) {
        Properties anims = new Properties();
        try {
            anims.load(this.getClass().getResourceAsStream(animationFile));
            for (Map.Entry<Object, Object> e : anims.entrySet()) {
                String animName = (String) e.getKey();
                String animFrames = (String) e.getValue();

                String[] args = animFrames.split(";");
                Animation anim = loadAnimation(
                        args[0],
                        args[1].equals("loop"),
                        args[2].substring("{".length(), args[2].length() - "}".length()).split("\\+"));
                animations.put(animName, anim);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create one {@link Animation} instance according to the prepared data.
     *
     * @param imageSrcPath image file where to extract frames
     * @param loop         set the looping attribute for the {@link Animation}
     *                     instance
     * @param framesDef    a list of frame definition <code>"x,y,w,h,t"</code>.
     * @return the corresponding initialized {@link Animation} instance.
     */
    public Animation loadAnimation(String imageSrcPath, boolean loop, String[] framesDef) {
        ResourceManager rm = (ResourceManager) SystemManager.get(ResourceManager.NAME);

        BufferedImage[] imgs = new BufferedImage[framesDef.length];
        long[] frameTimes = new long[framesDef.length];
        BufferedImage imageSource = rm.getImage(imageSrcPath);
        int i = 0;
        for (String f : framesDef) {
            String[] val = f.split(",");
            int x = Integer.valueOf(val[0]);
            int y = Integer.valueOf(val[1]);
            int w = Integer.valueOf(val[2]);
            int h = Integer.valueOf(val[3]);
            int frameTime = Integer.valueOf(val[4]);
            imgs[i] = imageSource.getSubimage(x, y, w, h);
            frameTimes[i] = frameTime;
            i++;
        }

        return new Animation(imgs, frameTimes).setLoop(loop);
    }

    /**
     * Retrieve a specific animation on its name from tha Animations cache.
     *
     * @param animKey the name of the animation to be retrieved.
     * @return the corresponding initialized {@link Animation} instance.
     */
    public Animation get(String animKey) {
        return animations.get(animKey);
    }
}
