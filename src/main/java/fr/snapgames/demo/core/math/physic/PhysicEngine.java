package fr.snapgames.demo.core.math.physic;

import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.scenes.Scene;
import fr.snapgames.demo.core.system.GameSystem;

import java.awt.*;

/**
 * A simple home-grown {@link PhysicEngine} to process all entities and make the
 * behavior near some realistic physic law (adapted and simplified ones.)
 * <p>
 * It is based on the {@link Entity#position} position,
 * {@link Entity#velocity} velocity attributes and the
 * {@link Entity#mass} from the
 * {@link Entity} class.
 * <p>
 * It is using a {@link World} object defining {@link World#getPlayArea()} and
 * the internal {@link World#getGravity()} value.
 * <p>
 * Each Entity has its own {@link Material} characteristics, influencing
 * elasticity, roughness, density.
 *
 * <blockquote><strong>INFO:</strong> the {@link PhysicEngine#TIME_FACTOR} is an
 * internal
 * reduction factor to adapt global speed of all object on screen.</blockquote>
 *
 * @author Frédéric Delorme
 * @see Entity
 * @see Material
 * @see World
 * @since 1.0.0
 */
public class PhysicEngine extends GameSystem {

    public static final String NAME = "PhysicEngine";

    static final double TIME_FACTOR = 0.045;
    private World world;

    /**
     * Initialize the Physic Engine with its parent node.
     *
     * @param game
     */
    public PhysicEngine(Game game) {

        super(game, NAME);
    }

    /**
     * Process all current game entities.
     * <p>
     * Entities are filtered on only active ones, ant sorted regarding their
     * priority.
     * After processing the new position, the entity is constrained to not be out
     * of the world play area ({@link World#getPlayArea()}), and apply a
     * {@link Material#elasticity} factor on it and changes
     * the velocity on the impacted axis.
     * </p>
     *
     * @param scene   the current Scene to be updated.
     * @param elapsed the elapsed time since previous update call.
     */
    public void update(Scene scene, long elapsed) {
        scene.getEntities().values().stream()
                .filter(e -> !(e instanceof Camera) && e.isActive())
                .sorted((e1, e2) -> e1.priority < e2.priority ? 1 : -1)
                .forEach(e -> {
                    updateEntity(e, elapsed);
                    if (!e.isRelativeToParent() && !e.isFixedToCamera()) {
                        constraintsEntity(e);
                    }
                });
    }

    /**
     * Apply World's play area ({@link World#getPlayArea()}) constrains on the
     * {@link Entity}.
     * <p>
     * In case of collision with play area borders, the {@link Material#elasticity}
     * factor is applied onto the
     * {@link Entity#velocity}, and {@link Entity#position} is corrected.
     * </p>
     *
     * @param e the Entity to be constrained.
     */
    private void constraintsEntity(Entity e) {
        Dimension playArea = (Dimension) getGame().getConfiguration().get(ConfigAttribute.PHYSIC_PLAY_AREA);
        e.contact = 0;
        if (e.position.x <= 0) {
            e.position.x = 0;
            e.velocity.x = -(e.getMaterial().elasticity * e.velocity.x);
            e.contact += 1;
        }
        if (e.position.y <= 0) {
            e.position.y = 0;
            e.velocity.y = -(e.getMaterial().elasticity * e.velocity.y);
            e.contact += 2;
        }
        if (e.position.x + e.width > playArea.width) {
            e.position.x = playArea.width - e.width;
            e.velocity.x = -(e.getMaterial().elasticity * e.velocity.x);
            e.contact += 4;
        }
        if (e.position.y + e.height > playArea.height) {
            e.position.y = playArea.height - e.height;
            e.velocity.y = -(e.getMaterial().elasticity * e.velocity.y);
            e.contact += 8;
        }
    }

    /**
     * Update of an individual {@link Entity} by computing its own
     *
     * @param e       the Entity to be updated.
     * @param elapsed the elapsed time since previous update call.
     */
    private void updateEntity(Entity e, long elapsed) {
        double time = elapsed * TIME_FACTOR;
        if (!e.isFixedToCamera() && e.getPhysicType() == PhysicType.DYNAMIC) {
            if (!e.isRelativeToParent()) {
                if (e.mass != 0) {
                    e.velocity.y = world.getGravity() * (elapsed * 0.5) * 10.0 / e.mass;
                }
                if (e.contact > 0) {
                    e.velocity.y *= e.getMaterial().friction;
                    e.velocity.y *= e.getMaterial().friction;
                }
                e.position.x += e.velocity.x * time;
                e.position.y += e.velocity.y * time;
            }
        }
        // update animation with next frame (if required)
        if (!e.currentAnimation.isEmpty()) {
            e.getAnimations().get(e.currentAnimation).update(elapsed);
        }
        // process attached behaviors
        if (!e.getBehaviors().isEmpty()) {
            e.getBehaviors().forEach(b -> b.update(elapsed, e));
        }

        e.update(elapsed);
    }

    /**
     * Set the {@link PhysicEngine}'s {@link World} instance to define gravity, play
     * area and more.
     *
     * @param world the {@link World} object defining the Physic limit for the
     *              {@link PhysicEngine}.
     */
    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Retrieve the current {@link World} instance used by the {@link PhysicEngine}
     * to constrain Entities.
     *
     * @return a {@link World} instance used by the PhysicEngine computation system.
     */
    public World getWorld() {
        return this.world;
    }
}
