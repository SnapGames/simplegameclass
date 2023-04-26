package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.math.physic.Material;

/**
 * A future object to be used into the PhysicEngine to define new constrains,
 * to apply effects on Entity intersecting with the Influencer area.
 * <p>
 * This {@link Influencer} would be able to apply new force (magnetic, wind,
 * etc...) on the
 * {@link Entity}, and dynamically change the default {@link Entity}'s
 * {@link Material}.
 *
 * @author Frédéric Delorme
 * @since 1.0.2
 */
public class Influencer extends Entity {

    public Influencer(String name) {
        super(name);
    }
}
