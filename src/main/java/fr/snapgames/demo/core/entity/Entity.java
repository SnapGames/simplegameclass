package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.gfx.animation.Animation;
import fr.snapgames.demo.core.gfx.animation.Animations;
import fr.snapgames.demo.core.math.physic.Material;

import java.awt.*;
import java.util.Map;

/**
 * Core Entity for all managed object on screen.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Entity extends AbstractEntity<Entity> {
    public Entity(String name, int x, int y, Color borderColor, Color fillColor) {
        super(name, x, y, borderColor, fillColor);
    }

    public Entity(String name) {
        super(name, 0, 0, null, null);
    }

    public String toString() {
        return "#" + this.getId() + ":" + this.getName();
    }

    public Material getMaterial() {
        return material;
    }

    public Map<String, Animation> getAnimations() {
        return animations;
    }
}
