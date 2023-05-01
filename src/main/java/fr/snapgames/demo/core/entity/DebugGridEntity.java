package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.math.physic.World;

import java.awt.*;

public class DebugGridEntity extends Entity {
    public int stepX, stepY;
    public World world;

    public DebugGridEntity(String name, int stepX, int stepY, World world) {
        super(name);
        this.world = world;
        this.stepX = stepX;
        this.stepY = stepY;
        setPriority(20);
    }
}
