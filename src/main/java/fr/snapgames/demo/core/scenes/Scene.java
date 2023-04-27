package fr.snapgames.demo.core.scenes;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.Renderer;

import java.util.Map;

/**
 * Define a {@link Scene} interface to b managed by the SceneManager.
 * <p>
 * The Lifecycle of the object is as bellow:
 *
 * <ul>
 *     <li><code>prepare()</code> Prepare the scene by loading some resources,</li>
 *     <li><code>create()</code> instantiate all te Scene objects like Entity, Particle, Camera,...</li>
 *     <li><code>getFocus()</code> when the Scene get focus from the game. it is useful to recenter the camera on the player,
 *     or rest some data,</li>
 *     <li><code>input()</code> process the user inputs even it s keyboard, mouse, gamepad, joystick, any input device,</li>
 *     <li><code>update()</code> used to process all entities. it is called by the
 *     {@link fr.snapgames.demo.core.math.physic.PhysicEngine} to process moves, detect collision, etc...</li>
 *     <li><code>draw()</code> this is where the {@link Renderer} play its role by drawing everything in the scene onto
 *     the screen.</li>
 *     <li><code>lostFocus()</code> is called just when a new scene must be activated, to store or hold thing in this scene.</li>
 *     <li><code>dispose()</code> when the scene must be deleted from memory, thus can deal with some resources, os some
 *     saving operation.</li>
 * </ul>
 *
 * @author Frédéric Delorme
 * @since 1.0.2
 */
public interface Scene {
    /**
     * Retrieve the Scene name for SceneManager
     *
     * @return
     */
    String getName();

    /**
     * Prepare {@link Scene} by loading some resources (Font, Image, etc....) and instantiating some required objects.
     *
     * @param g the parent {@link Game} of that {@link Scene}.
     */
    void prepare(Game g);

    /**
     * the {@link Scene#create(Game)} method is called after {@link Scene#prepare(Game)} to instantiate all
     * the scenes {@link fr.snapgames.demo.core.entity.Entity}, and populate the play area.
     *
     * @param g the parent {@link Game} of that {@link Scene}.
     */
    void create(Game g);

    /**
     * update all the Scene according to the fact that {@link fr.snapgames.demo.core.entity.Entity}s are
     * updated by the {@link fr.snapgames.demo.core.math.physic.PhysicEngine}.
     * <p>
     * Only Scene relative information must be updated here.
     *
     * @param g       the parent {@link Game} of that {@link Scene}.
     * @param elapsed the elapsed time since previous call (in ns).
     * @param data    a map of metadata than is useful only for debug purpose.
     */
    void update(Game g, long elapsed, Map<String, Object> data);

    /**
     * This where specific draw operation relative to this scene can be achieved. Called by the {@link Renderer} service.
     * <p>
     * Remember that any {@link fr.snapgames.demo.core.entity.Entity} is draw by the {@link Renderer}.
     *
     * @param g    the parent {@link Game} of that {@link Scene}.
     * @param r    the {@link Renderer} instance calling this {@link Scene#draw(Game, Renderer, Map)} method.
     * @param data a map of metadata than is useful only for debug purpose.
     */
    void draw(Game g, Renderer r, Map<String, Object> data);

    /**
     * this method is called just before another scene is going to be activated This {@link Scene} lost the game focus.
     *
     * @param g the parent {@link Game} of that {@link Scene}.
     */
    default void lostFocus(Game g) {
        // Nothing specific jere.
    }

    /**
     * When the Scene has juste been activated, the obtainFocus method is called to actiavte,
     * un-hold some things in the scene.
     *
     * @param g the parent {@link Game} of that {@link Scene}.
     */
    default void obtainFocus(Game g) {
        // Nothing specific here.
    }

    /**
     * The {@link Scene#dispose(Game)} is called to release all loaded resources and or objects in memory,
     * freeing resources.
     *
     * @param g the parent {@link Game} of that {@link Scene}.
     */
    void dispose(Game g);

    /**
     * Add an {@link Entity} to the {@link Scene}.
     *
     * @param e the {@link Entity} to be added.
     */
    void add(Entity e);

    /**
     * Add a {@link Behavior} names behaviorName to the {@link Scene}.
     *
     * @param behaviorName the internal name for this behavior.
     * @param behavior     the Behavior to be added.
     */
    void add(String behaviorName, Behavior<?> behavior);

    /**
     * Retrieve a {@link Behavior} thanks to its internal name.
     *
     * @param behaviorName
     * @return the  {@link Behavior} instance corresponding to the behaviorName.
     */
    Behavior<?> get(String behaviorName);

    /**
     * retrieve the Scene {@link Entity} map.
     *
     * @return a Map of {@link Entity} instances.
     */
    Map<String, Entity> getEntities();

    /**
     * Retrieve the {@link Behavior} map.
     *
     * @return a Map of {@link Behavior} instances.
     */
    Map<String, Behavior<?>> getBehaviors();
}
