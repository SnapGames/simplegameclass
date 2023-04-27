package fr.snapgames.demo.core.scenes;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.system.GameSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SceneManager extends GameSystem {
    private Map<String, Scene> scenes = new HashMap<>();
    public static final String NAME = "SceneManager";

    private Scene activeScene;

    public SceneManager(Game g) {
        super(g, NAME);
    }

    public void add(Scene scene) {
        scenes.put(scene.getName(), scene);
    }

    public void activate(String name) {
        if (Optional.ofNullable(activeScene).isPresent()) {
            this.activeScene.lostFocus(getGame());
        }
        if (scenes.containsKey(name)) {
            this.activeScene = scenes.get(name);
            this.activeScene.prepare(getGame());
            this.activeScene.create(getGame());
            this.activeScene.obtainFocus(getGame());
        } else {
            System.err.printf("ERROR: The scene %s does not exists%n", name);
        }
    }

    public Scene getActiveScene() {
        return activeScene;
    }
}
