package fr.snapgames.demo.core.behaviors.sfx;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.ParticleBehavior;
import fr.snapgames.demo.core.entity.EntityType;
import fr.snapgames.demo.core.entity.Particle;
import fr.snapgames.demo.core.math.physic.PhysicType;
import fr.snapgames.demo.core.math.physic.World;
import fr.snapgames.demo.core.scenes.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link ParticleBehavior} implements a Snow simulations.
 * Snowflakes (DOT) are falling from sky in a continuous way.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public class SnowBehavior implements ParticleBehavior<Particle> {
    private final Scene scene;
    private int batch = 10;
    Dimension playArea;
    List<Particle> drops = new ArrayList<>();

    boolean run = false;

    public String getName() {
        return "Snow";
    }

    public SnowBehavior(Scene scene, World world, int batch) {
        this.scene = scene;
        this.playArea = world.getPlayArea();
        this.batch = batch;
    }

    @Override
    public void update(long elapsed, Particle e) {
        e.setSize(playArea.width, playArea.height);
        e.setPhysicType(PhysicType.STATIC);
        // add drops to the particles system
        double i = 0.0;
        double maxBatch = ((this.batch * 0.5) + (this.batch * Math.random() * 0.5));
        while (i < maxBatch && drops.size() < e.getNbParticles()) {
            Particle p = create(e);
            p.setPosition(
                    playArea.width * Math.random(),
                    0);
            drops.add(p);
            i += 1.0;

        }
        drops.stream().forEach(p -> {
            if (p.position.y >= playArea.height - 1) {
                p.setPosition(playArea.width * Math.random(),
                        0);
                p.setActive(run);
            }
        });
    }

    @Override
    public Particle create(Particle parent) {
        Particle pChild = (Particle) new Particle(parent.getName() + "_spark_" + parent.getCurrentIndex())
                .setType(EntityType.DOT)
                .setPhysicType(PhysicType.DYNAMIC)
                .setSize(1, 1)
                .setFillColor(Color.WHITE)
                .setMass(4000.01)
                .setVelocity(0.8 - (Math.random() * 1.6), Math.random() * 0.0009)
                .setRelativeToParent(false);
        parent.addChild(pChild);
        scene.add(pChild);
        return pChild;
    }

    @Override
    public void start() {
        this.run = true;
    }

    @Override
    public void stop() {
        this.run = false;
    }
}
