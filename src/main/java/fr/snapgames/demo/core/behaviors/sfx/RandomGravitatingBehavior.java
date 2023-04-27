package fr.snapgames.demo.core.behaviors.sfx;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.entity.Entity;

public class RandomGravitatingBehavior implements Behavior<Entity> {
    private final double speed;
    private final double radius1;
    private final double y;
    private final double x;

    public RandomGravitatingBehavior(double x, double y, double radius1) {
        this.x = x;
        this.y = y;
        this.radius1 = radius1;
        this.speed = -0.5 + Math.random();
    }

    @Override
    public void update(long elapsed, Entity e) {
        double life = (Double) e.getAttribute("life", Double.valueOf(Math.PI * 2.0));
        life += 0.05 * speed;
        if (life > Math.PI * 2) {
            life = 0;
        }
        if (life < 0) {
            life = Math.PI * 2.0;
        }
        e.position.x = x + (Math.cos(life) * radius1);
        e.position.y = y + (Math.sin(life) * radius1)
                + (Math.sin(life * radius1 * 0.25) * 8.0)
                + (Math.sin(life * radius1 * 0.5) * 4.0);
        e.setAttribute("life", life);
    }

}
