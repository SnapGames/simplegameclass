package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.ParticleBehavior;

import java.util.Optional;

/**
 * A new Entity to display a bunch of particles.
 *
 * <p>
 * The {@link Particle} entity is a group of child entity sharing the same
 * behavior.
 * And the {@link Particle} behavior is a new flavor of the {@link Behavior}'s
 * one adding new methods: the {@link ParticleBehavior}.
 */
public class Particle extends Entity {
    int nbParticles = 0;

    public Particle(String name) {
        super(name, 0, 0, null, null);
        this.nbParticles = -1;
        super.setRelativeToParent(true);
    }

    public Particle(String name, int x, int y, int nbParticles) {
        super(name, x, y, null, null);
        this.type = EntityType.NONE;
        this.setMass(0.0);
        this.nbParticles = nbParticles;
    }

    public void createParticle(Game g, ParticleBehavior<Particle> pb) {
        if (Optional.ofNullable(pb).isPresent()) {
            addChild(pb.create(this));
        }
    }

    public int getNbParticles() {
        return nbParticles;
    }
}
