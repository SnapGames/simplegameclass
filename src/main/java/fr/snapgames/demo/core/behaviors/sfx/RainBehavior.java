package fr.snapgames.demo.core.behaviors.sfx;

import fr.snapgames.demo.core.behaviors.ParticleBehavior;
import fr.snapgames.demo.core.entity.EntityType;
import fr.snapgames.demo.core.entity.Particle;
import fr.snapgames.demo.core.math.physic.PhysicType;
import fr.snapgames.demo.core.math.physic.World;
import fr.snapgames.demo.core.scenes.Scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@link RainBehavior} is {@link ParticleBehavior} implementation to
 * simulate Rain.
 * rain drop will fall from sky (upper play area) to ground (lower play area).
 * Rain drops are DOT with a blue half-transparent color.
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public class RainBehavior implements ParticleBehavior<Particle> {
    private final Scene scene;
    private final double speed;
    private int batch = 10;
    Dimension playArea;
    boolean run = false;
    List<Particle> drops = new ArrayList<>();
    private long nbActive = 0;

    private long dropTime = 0;

    private long internalTime = 0;

    private final Color dropColor = new Color(0.4f, 0.7f, 0.9f, 0.5f);

    public String getName() {
        return "Rain";
    }

    /**
     * Generate batch rain drop every dropTime millisecond.
     *
     * @param world    parent World object to define PLay area
     * @param batch    number of drop to be generated on the dropTime delay.
     * @param dropTime the delay in the batch rain drops must be generated.
     */
    public RainBehavior(Scene game, World world, int batch, int dropTime, double dropSpeed) {
        this.scene = game;
        this.playArea = world.getPlayArea();
        this.batch = batch;
        this.dropTime = dropTime;
        this.speed = dropSpeed;
    }

    @Override
    public void update(long elapsed, Particle e) {
        e.setSize(playArea.width, playArea.height);
        e.setPhysicType(PhysicType.STATIC);
        internalTime += elapsed;
        // add drops to the particles system
        double i = 0.0;
        if (run && internalTime > dropTime) {
            double maxBatch = ((this.batch * 0.5) + (this.batch * Math.random() * 0.5));
            while (i < maxBatch && nbActive < e.getNbParticles()) {
                create(e);
                i += 1.0;
            }
            nbActive = drops.stream().filter(p -> p.isActive()).count();
            internalTime = 0;
        }
    }

    @Override
    public Particle create(Particle parent) {
        Particle pChild = null;
        if (drops.size() < parent.getNbParticles()) {
            pChild = (Particle) new Particle(parent.getName() + "_drop_" + parent.getCurrentIndex())
                    .setType(EntityType.LINE)
                    .setPhysicType(PhysicType.DYNAMIC)
                    .setSize(1, 1)
                    .setPosition(
                            playArea.width * Math.random(),
                            0)
                    .setBorderColor(dropColor)
                    .setMass(1000.0)
                    .setVelocity(0.5 - Math.random(), speed)
                    .setRelativeToParent(false)
                    .add(new RainDropBehavior(playArea))
                    .setActive(true);
            parent.addChild(pChild);
            scene.add(pChild);
            drops.add(pChild);
        } else {
            Optional<Particle> existingParticle = drops.stream().filter(p -> !p.isActive()).findFirst();
            if (existingParticle.isPresent()) {
                pChild = existingParticle.get();
                pChild.setActive(true);
            }
        }
        return pChild;
    }

    public void start() {
        this.run = true;
    }

    public void stop() {
        this.run = false;
    }
}
