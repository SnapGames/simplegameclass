package fr.snapgames.demo.core.behaviors;

import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.entity.Particle;

public interface ParticleBehavior<T extends Entity> extends Behavior<T> {
    String getName();

    Particle create(T parent);

    void start();

    void stop();
}
