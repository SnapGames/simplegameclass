package fr.snapgames.demo.core.math.physic;

import fr.snapgames.demo.core.entity.Entity;

/**
 * The {@link Material} class is defining some physic attributes to be applied
 * on some {@link Entity},
 * and used in the {@link PhysicEngine} Newton's laws processing to move
 * {@link Entity}.
 * <p>
 * It is used to define common physic attributes like
 * {@link Material#elasticity}, {@link Material#density}
 * and {@link Material#friction}.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Material {

    String name;
    double density;
    double elasticity;
    public double friction;

    public Material(String name, double d, double e, double f) {
        this.name = name;
        this.density = d;
        this.elasticity = e;
        this.friction = f;
    }
}
