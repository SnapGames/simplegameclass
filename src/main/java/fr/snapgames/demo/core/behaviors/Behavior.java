package fr.snapgames.demo.core.behaviors;

import fr.snapgames.demo.core.io.UserInput;

import java.awt.*;

public interface Behavior<Entity> {
    default void input(UserInput ui, Entity e) {

    }

    default void update(long elapsed, Entity e) {

    }

    default void draw(Graphics2D g, Entity e) {

    }
}
