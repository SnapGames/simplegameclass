package fr.snapgames.demo.core.math.physic;

import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.entity.Influencer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link World} object defining the limit of the {@link PhysicEngine}
 * universe here the {@link Entity} will evolve.
 * <p>
 * It will contain a default a gravity, and a play area where Entity moves.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public class World {
    private double gravity = 0.981;
    private Dimension playArea;

    java.util.List<Influencer> influencers = new ArrayList<>();

    public World(double g, Dimension pa) {
        this.gravity = g;
        this.playArea = pa;
    }

    public Dimension getPlayArea() {
        return this.playArea;
    }

    public double getGravity() {
        return this.gravity;
    }

    public World add(Influencer i) {
        this.influencers.add(i);
        return this;
    }

    public List<Influencer> getInfluencers() {
        return influencers;
    }
}
