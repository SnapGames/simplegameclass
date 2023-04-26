package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.math.physic.PhysicType;

import java.awt.*;

/**
 * The {@link Camera} is a specific {@link Entity} that is used to let the
 * screen view follow a defined target.
 * As such a Caemra in the a movie, it will follow a target with smooth moves.
 * <p>
 * Defining a camera is notheting than this sample of code:
 *
 * <pre>
 * public void create() {
 *     // ...
 *     Camera cam = new Camera("myCam")
 *             .setTarget(player)
 *             .setTween(0.04)
 *             .setViewport(vp);
 *     add(cam);
 *     // ...
 * }
 * </pre>
 */
public class Camera extends Entity {
    Entity target;
    double tween;

    double rotation = 0.0;
    Dimension viewport;

    public Camera(String name) {
        super(name, 0, 0, null, null);
    }

    public Camera setTarget(Entity t) {
        this.target = t;
        return this;
    }

    public Camera setTween(double tw) {
        this.tween = tw;
        return this;
    }

    public Camera setViewport(Dimension vp) {
        this.viewport = vp;
        return this;
    }

    public Camera setRotation(double r) {
        this.rotation = r;
        return this;
    }

    public void preDraw(Graphics2D g) {
        g.translate(-position.x, -position.y);
        g.rotate(-rotation);
    }

    public void postDraw(Graphics2D g) {

        g.rotate(rotation);
        g.translate(position.x, position.y);
    }

    public void update(long elapsed) {
        this.position.x += Math
                .ceil((target.position.x + (target.width * 0.5) - ((viewport.getWidth()) * 0.5) - this.position.x)
                        * tween * Math.min(elapsed, 0.8));
        this.position.y += Math
                .ceil((target.position.y + (target.height * 0.5) - ((viewport.getHeight()) * 0.5) - this.position.y)
                        * tween * Math.min(elapsed, 0.8));
    }

    /**
     * Check if the {@link Entity} e is in the field of view (viewport) of the
     * {@link Camera}.
     *
     * @param e the {@link Entity} to be field of view checked.
     * @return true if {@link Entity} is in the FOV.
     */
    public boolean isInFOV(Entity e) {
        if (e.isFixedToCamera() || e.getPhysicType().equals(PhysicType.STATIC)) {
            return true;
        } else if (e.isRelativeToParent()) {
            return e.position.x + e.getParent().position.x >= position.x
                    && e.position.x + e.getParent().position.x <= position.x + viewport.width
                    && e.position.y + e.getParent().position.y >= position.y
                    && e.position.y + e.getParent().position.y <= position.y + viewport.height;
        } else {
            return e.position.x >= position.x && e.position.x <= position.x + viewport.width
                    && e.position.y >= position.y && e.position.y <= position.y + viewport.height;
        }

    }
}
