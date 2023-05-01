package fr.snapgames.demo.core.gfx.plugins;

import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.DrawPlugin;
import fr.snapgames.demo.core.gfx.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * A Default implementation for the {@link DrawPlugin}, to be used on any basic
 * {@link Entity},
 * and to be extending for any common {@link Entity} processing.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public abstract class DefaultDrawPlugin<T extends Entity> implements DrawPlugin<T> {
    @Override
    public abstract Class<T> getClassName();

    @Override
    public void draw(Renderer r, Graphics2D g, T e) {
        double x = e.position.x;
        double y = e.position.y;
        if (e.isRelativeToParent()) {
            x = e.getParent().position.x + e.position.x;
            y = e.getParent().position.y + e.position.y;
        }

        switch (e.type) {
            // draw a simple rectangle
            case RECTANGLE -> {
                g.setColor(e.fillColor);
                g.fillRect((int) x, (int) y, (int) e.width, (int) e.height);
                g.setColor(e.borderColor);
                g.drawRect((int) x, (int) y, (int) e.width, (int) e.height);
            }
            // draw a line
            case LINE -> {
                g.setColor(e.borderColor);
                Stroke bs = g.getStroke();
                g.setStroke(new BasicStroke((float) e.width));
                g.drawLine((int) x, (int) y, (int) (e.position.x + e.velocity.x),
                        (int) (e.position.y + e.velocity.y));
                g.setStroke(bs);
            }
            // draw an ellipse
            case ELLIPSE -> {
                g.setColor(e.fillColor);
                g.fillOval((int) x, (int) y, (int) e.width, (int) e.height);
                g.setColor(e.borderColor);
                g.drawOval((int) x, (int) y, (int) e.width, (int) e.height);
            }
            // draw a Dot (dot is a width x width Ellipse)
            case DOT -> {
                g.setColor(e.fillColor);
                g.fillOval((int) x, (int) y, (int) e.width, (int) e.width);
            }
            // draw the entity corresponding image or current animation image frame
            case IMAGE -> {
                BufferedImage img = e.image;
                if (!e.currentAnimation.equals("")) {
                    img = e.getAnimations().get(e.currentAnimation).getFrame();
                }
                if (Optional.ofNullable(img).isPresent()) {
                    if (e.direction >= 0) {
                        g.drawImage(
                                img,
                                (int) x, (int) y,
                                (int) e.width, (int) e.height,
                                null);
                    } else {
                        g.drawImage(
                                img,
                                (int) (x + e.width), (int) y,
                                (int) -e.width, (int) e.height,
                                null);
                    }
                    e.setSize(img.getWidth(), img.getHeight());
                    e.updateBBox();
                }
            }
            case NONE -> {
                // Nothing to do
            }
            default -> {
                System.err.printf("ERROR: Unable to draw the entity %s%n", e.getName());
            }
        }

    }
}
