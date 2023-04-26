package fr.snapgames.demo.core.math.physic;

import fr.snapgames.demo.core.entity.Entity;

/**
 * {@link PhysicType} for any {@link Entity}. It defines the Physic Computation
 * applied to the
 * {@link Entity} according to the fact it os static of dynamic.
 *
 * @author Frédéric Delorme
 * @see PhysicEngine
 * @see Entity#physicType
 * @since 1.0.1
 */
public enum PhysicType {
    /**
     * A STATIC {@link Entity} will not be updated by the {@link PhysicEngine}
     * computation,
     * only the entity's behaviors will be updated.
     */
    STATIC,
    /**
     * a DYNAMIC {@link Entity} will be impacted by the simplified Newton's laws
     * computation performed by the {@link PhysicEngine}.
     */
    DYNAMIC
}
