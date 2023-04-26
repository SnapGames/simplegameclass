package fr.snapgames.demo.core.behaviors.sfx;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.behaviors.ParticleBehavior;
import fr.snapgames.demo.core.entity.Particle;

import java.awt.*;

/**
 * This {@link ParticleBehavior} implements the Falling behavior of a rain drop.
 * Teh Drop will fall from sky to ground.
 * As soon this drop reach the ground, it is deactivated.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public class RainDropBehavior implements Behavior<Particle> {
    private final Dimension playArea;

    public RainDropBehavior(Dimension playArea) {
        this.playArea = playArea;
    }

    @Override
    public void update(long elapsed, Particle p) {
        if (p.position.y >= playArea.height - 1) {
            p.setPosition(playArea.width * Math.random(),
                    0);
            p.setActive(false);
        }
    }
}
