package fr.snapgames.demo.core.gfx.plugins;

import fr.snapgames.demo.core.entity.Particle;

public class ParticleDrawPlugin extends DefaultDrawPlugin<Particle> {

    @Override
    public Class<Particle> getClassName() {
        return Particle.class;
    }
}
