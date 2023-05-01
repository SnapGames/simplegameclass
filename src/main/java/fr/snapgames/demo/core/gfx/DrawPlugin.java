package fr.snapgames.demo.core.gfx;

import fr.snapgames.demo.core.entity.Entity;

import java.awt.*;

/**
 * The {@link DrawPlugin} interface define a common way to draw any
 * {@link Entity}.
 * <p>
 * It will be used by any implementation to tell how to draw a specific object
 * inheriting from {@link Entity}.
 *
 * @param <T> the class inheriting from {@link Entity} this {@link DrawPlugin}
 *            implementation is defined for.
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public interface DrawPlugin<T extends Entity> {
    /**
     * Class of the object that can be drawn by that DrawPlugin.
     *
     * @return a Class specification.
     */
    Class<T> getClassName();

    /**
     * Implementation of the draw for the defined class
     *
     * @param r the parent Renderer
     * @param g the Graphics2D interface to be used as "pencil"
     * @param t the T extending {@link Entity} instance to be drawn.
     */
    default void draw(Renderer r, Graphics2D g, T t) {
    }

    default void drawDebug(Renderer r, Graphics2D g, T t) {
        // draw debug info if required
        if (r.isDebugAtLeast(2)) {
            r.drawDebugEntityInfo(g, t);
        }
    }
}
