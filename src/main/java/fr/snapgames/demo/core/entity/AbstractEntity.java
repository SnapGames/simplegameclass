package fr.snapgames.demo.core.entity;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.gfx.animation.Animation;
import fr.snapgames.demo.core.math.physic.Material;
import fr.snapgames.demo.core.math.physic.PhysicType;
import fr.snapgames.demo.core.math.Vector2D;
import fr.snapgames.demo.core.structure.Node;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AbstractEntity} is the master {@link Entity} Object which all Entity's
 * will inherit from, by defining all the internal behavior and processing for
 * an entity.
 *
 * <p>
 * To be used, an inheritance must specify its type to the
 * {@link AbstractEntity}
 * to get the right returned type:
 *
 *
 * <p>
 * The MyEntity type will override any of the existing methods from
 * AbstractEntity,
 * and must have a Constructor call the parent one, here a simplified one with
 * entity's name only.
 *
 * <pre>
 * public class MyEntity extends AbstractEntity<MyEntity> {
 *
 *     public MyEntity(String name) {
 *         super(name, 0, 0, null, null);
 *     }
 *     // ...
 * }
 * </pre>
 *
 * @author Frédéric Delorme
 * @since 1.0.1
 */
public abstract class AbstractEntity<T extends Node<T>> implements Node<T> {
    private static long index = 0;
    private final long id = ++index;
    public double rotation = 0.0;
    private String name = "default_" + id;
    public int priority = 0;
    public EntityType type = EntityType.RECTANGLE;

    public Vector2D position;

    public Vector2D velocity;
    public double width = 16;
    public double height = 16;

    Shape bbox;
    public double mass = 1.0;
    public BufferedImage image = null;
    public Color borderColor = Color.WHITE;
    public Color fillColor = Color.BLUE;
    public int direction = 0;
    Material material = new Material("default", 1.0, 0.60, 0.998);
    public int contact;

    boolean relativeToParent = false;

    T parent;
    private final java.util.List<T> child = new ArrayList<>();
    Map<String, Object> attributes = new HashMap<>();

    Map<String, Animation> animations = new HashMap<>();
    public String currentAnimation = "";

    java.util.List<Behavior<T>> behaviors = new ArrayList<>();
    private boolean fixedToCamera;
    private boolean active = true;

    private long duration = -1;
    private long live = 0;
    private PhysicType physicType = PhysicType.DYNAMIC;

    public AbstractEntity(String name, int x, int y, Color borderColor, Color fillColor) {
        this.name = name;
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.borderColor = borderColor;
        this.fillColor = fillColor;
        updateBBox();
    }

    public void updateBBox() {
        if (type.equals(EntityType.ELLIPSE)) {
            this.bbox = new Ellipse2D.Double(position.x, position.y, width, height);
        } else {
            this.bbox = new Rectangle2D.Double(position.x, position.y, width, height);
        }
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public T setMaterial(Material mat) {
        this.material = mat;
        return (T) this;
    }

    public T setMass(double m) {
        this.mass = m;
        return (T) this;
    }

    public T setFixedToCamera(boolean f) {
        this.fixedToCamera = f;
        return (T) this;
    }

    public T add(String name, Animation a) {
        this.type = EntityType.IMAGE;
        this.animations.put(name, a);
        if (currentAnimation.equals("")) {
            currentAnimation = name;
            this.width = animations.get(currentAnimation).getFrame().getWidth();
            this.height = animations.get(currentAnimation).getFrame().getHeight();
        }
        return (T) this;
    }

    public T setAnimation(String name) {
        this.currentAnimation = name;
        return (T) this;
    }

    public T setSize(double w, double h) {
        this.width = w;
        this.height = h;
        return (T) this;
    }

    public java.util.List<T> getChild() {
        return child;
    }

    public T addChild(T c) {
        c.setParent((T) this);
        this.child.add(c);
        return (T) this;
    }

    public T getParent() {
        return parent;
    }

    public T add(Behavior<?> b) {
        this.behaviors.add((Behavior<T>) b);
        return (T) this;
    }

    public T setType(EntityType t) {
        this.type = t;
        return (T) this;
    }

    public T setParentRelative(boolean pr) {
        this.relativeToParent = pr;
        return (T) this;
    }

    public T setAttribute(String key, Object value) {
        attributes.put(key, value);
        return (T) this;
    }

    public Object getAttribute(String key, Object defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }

    public boolean isFixedToCamera() {
        return fixedToCamera;
    }

    public boolean isRelativeToParent() {
        return relativeToParent;
    }

    public boolean isActive() {
        return this.active;
    }

    public T setPriority(int p) {
        this.priority = p;
        return (T) this;
    }

    public java.util.List<Behavior<T>> getBehaviors() {
        return behaviors;
    }

    public T setRelativeToParent(boolean rtp) {
        this.relativeToParent = rtp;
        return (T) this;
    }

    public java.util.List<String> getDebugInfo() {
        List<String> info = new ArrayList<>();
        info.add(String.format("#%d:%s", id, name));
        if (isRelativeToParent()) {
            info.add(String.format("offset:%s", position));
        } else {
            info.add(String.format("pos:%s", position));
        }
        info.add(String.format("sz :%3.02f,%3.02f", width, height));
        info.add(String.format("spd:%s", velocity));
        info.add(String.format("anm:%s", currentAnimation));
        info.add(String.format("life:%s", duration > -1 ? live + "/" + duration : "n/a"));
        return info;
    }

    public T setDuration(long d) {
        this.duration = d;
        if (duration > 0) {
            active = true;
        }
        return (T) this;
    }

    public T setPosition(double x, double y) {
        this.position.x = x;
        this.position.y = y;
        return (T) this;
    }

    public void update(long elapsed) {
        if (duration != -1) {
            live -= elapsed;
            if (live < 0) {
                live = 0;
                active = false;
            }
        }
        updateBBox();
    }

    public T setFillColor(Color color) {
        this.fillColor = color;
        return (T) this;
    }

    public T setBorderColor(Color bc) {
        this.borderColor = bc;
        return (T) this;
    }

    public T setVelocity(double dx, double dy) {
        this.velocity.x = dx;
        this.velocity.y = dy;
        return (T) this;
    }

    public T serRotation(double r) {
        this.rotation = r;
        return (T) this;
    }

    @Override
    public T setParent(T p) {
        this.parent = p;
        return (T) this;
    }

    public PhysicType getPhysicType() {
        return this.physicType;
    }

    public T setPhysicType(PhysicType pt) {
        this.physicType = pt;
        return (T) this;
    }

    public long getCurrentIndex() {
        return index;
    }

    public T setImage(BufferedImage image) {
        this.image = image;
        setType(EntityType.IMAGE);
        setSize(image.getWidth(), image.getHeight());
        updateBBox();
        return (T) this;
    }

    public T setActive(boolean active) {
        this.active = active;
        return (T) this;
    }

    public void remove(Behavior<?> rb) {
        behaviors.remove(rb);
    }
}
