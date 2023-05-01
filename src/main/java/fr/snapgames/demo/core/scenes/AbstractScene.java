package fr.snapgames.demo.core.scenes;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.configuration.Configuration;
import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.Renderer;
import fr.snapgames.demo.core.io.UserInput;
import fr.snapgames.demo.core.io.resource.ResourceManager;
import fr.snapgames.demo.core.math.physic.PhysicEngine;
import fr.snapgames.demo.core.system.SystemManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractScene implements Scene {

    protected final Map<String, Entity> entities = new ConcurrentHashMap<>();

    protected final Map<String, Camera> cameras = new ConcurrentHashMap<>();

    protected final Map<String, Behavior<?>> behaviors = new ConcurrentHashMap<>();

    protected final Game game;
    protected Renderer renderer;

    protected Configuration config;
    protected PhysicEngine physicEngine;
    protected ResourceManager resourceManager;
    protected UserInput userInput;
    private Font debugFont;

    protected AbstractScene(Game g) {
        this.game = g;
        this.config = g.getConfiguration();
        this.physicEngine = (PhysicEngine) SystemManager.get(PhysicEngine.NAME);
        this.resourceManager = (ResourceManager) SystemManager.get(ResourceManager.NAME);
        this.userInput = (UserInput) SystemManager.get(UserInput.NAME);
        this.renderer = (Renderer) SystemManager.get(Renderer.NAME);
    }

    @Override
    public void prepare(Game g) {
        renderer.setDebugFont(resourceManager.getFont("/fonts/lilliput steps.ttf").deriveFont(8.5f));
    }

    @Override
    public void create(Game g) {

    }

    @Override
    public void add(Entity entity) {
        if (entity instanceof Camera) {
            renderer.setCamera((Camera) entity);
            cameras.put(entity.getName(), (Camera) entity);
        }
        entities.put(entity.getName(), entity);
    }

    @Override
    public void add(String key, Behavior<?> behavior) {
        this.behaviors.put(key, behavior);
    }

    @Override
    public Behavior<?> get(String behaviorName) {
        return this.behaviors.get(behaviorName);
    }

    public Map<String, Behavior<?>> getBehaviors() {
        return this.behaviors;
    }

    @Override
    public Map<String, Entity> getEntities() {
        return entities;
    }
}
