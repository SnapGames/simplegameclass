package fr.snapgames.demo.core.gfx.plugins;

import fr.snapgames.demo.core.entity.Entity;

public class EntityDrawPlugin extends DefaultDrawPlugin<Entity> {

    @Override
    public Class<Entity> getClassName() {
        return Entity.class;
    }
}
