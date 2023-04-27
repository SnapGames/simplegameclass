package fr.snapgames.demo.core;

import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.configuration.Configuration;
import fr.snapgames.demo.core.gfx.Renderer;
import fr.snapgames.demo.core.io.UserInput;
import fr.snapgames.demo.core.io.resource.ResourceManager;
import fr.snapgames.demo.core.math.physic.PhysicEngine;
import fr.snapgames.demo.core.scenes.Scene;
import fr.snapgames.demo.core.scenes.SceneManager;
import fr.snapgames.demo.core.system.SystemManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Main Game class application with all its subclasses, encapsulating services
 * and entities.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Game {

    protected Configuration config;
    protected ResourceManager resourceManager;
    protected UserInput userInput;
    protected PhysicEngine physicEngine;
    protected Renderer renderer;
    private boolean exit;
    private boolean pause;

    private int debug;
    private final int meteoValue = 0;
    private Scene currentScene;
    private SceneManager sceneManager;

    public Game(String[] args, String pathToConfigPropsFile) {
        config = new Configuration(pathToConfigPropsFile, args);
        initialize();
    }

    public void initialize() {

        resourceManager = new ResourceManager(this);

        SystemManager.add(resourceManager);
        physicEngine = new PhysicEngine(this);
        SystemManager.add(physicEngine);
        renderer = new Renderer(this);
        SystemManager.add(renderer);
        userInput = new UserInput(this);
        SystemManager.add(userInput);

        sceneManager = new SceneManager(this);
        SystemManager.add(sceneManager);
        // set specific input handler for renderer window.
        renderer.setUserInput(userInput);
        // define icon for window.
        renderer.setWindowIcon(resourceManager.getImage("/images/sg-logo-image.png"));

        this.debug = (int) config.get(ConfigAttribute.DEBUG);
    }

    public void run() {
        System.out.printf("INFO: Main program started%n");
        Map<String, Object> context = new HashMap<>();
        prepare(context);
        System.out.printf("INFO: Scene prepared%n");
        create(context);
        System.out.printf("INFO: Scene created%n");
        loop();
        dispose();
        System.out.printf("INFO: Main program ended%n");
    }

    protected void prepare(Map<String, Object> context) {
        // defined in the Game inheriting class
    }

    protected void create(Map<String, Object> context) {
        // defined in the Game inheriting class
    }

    private void loop() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        long elapsed = 0;
        double timeFrame = 1000.0 / 60.0;

        int updates = 0, frames = 0;
        int fps = 0;
        int ups = 0;

        int internalTimeFrames = 0;
        long internalTime = 0;

        Map<String, Object> stats = new HashMap<>();

        while (!exit) {
            startTime = System.currentTimeMillis();
            elapsed = startTime - endTime;
            // detect and process user input
            currentScene = sceneManager.getActiveScene();
            input(currentScene);
            // update all entities
            if (!pause) {
                physicEngine.update(currentScene, elapsed);
                renderer.update(currentScene, elapsed);
                updates++;
                internalTimeFrames += elapsed;
            }

            // prepare statistics
            prepareStats(fps, ups, internalTime, stats);

            // render all entities
            renderer.draw(currentScene, stats);

            // compute some stats
            frames++;
            internalTime += elapsed;
            if (internalTimeFrames > 1000) {
                ups = updates;
                fps = frames;
                frames = 0;
                updates = 0;
                internalTimeFrames = 0;
            }

            waitForMs((int) (timeFrame - elapsed));

            endTime = startTime;
        }
    }

    private void prepareStats(int fps, int ups, long internalTime, Map<String, Object> stats) {
        final String[] meteoTitle = new String[]{"none", "Rain", "Snow"};
        stats.put("dbg", getDebugLevel());
        stats.put("obj", currentScene.getEntities().size());
        stats.put("cam", renderer.getCamera() != null ? renderer.getCamera().getName() : "none");
        stats.put("fps", fps);
        stats.put("ups", ups);
        stats.put("time", formatTime(internalTime));
        stats.put("meteo", meteoTitle[meteoValue]);

        stats.put("pause", isPause());
    }

    public static String formatTime(long millis) {
        long seconds = Math.round((double) millis / 1000);
        long hours = TimeUnit.SECONDS.toHours(seconds);
        if (hours > 0)
            seconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = seconds > 0 ? TimeUnit.SECONDS.toMinutes(seconds) : 0;
        if (minutes > 0)
            seconds -= TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void waitForMs(int ms) {
        if (ms < 0) {
            ms = 1;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.printf("ERROR: Unable to wait some ms %s%n", e.getMessage());
        }
    }

    private void input(Scene scene) {
        scene.getEntities().values().stream()
                .filter(e -> e.isActive() && !e.getBehaviors().isEmpty())
                .forEach(e -> {
                    e.getBehaviors().forEach(b -> b.input(userInput, e));
                });
        // process attached behaviors
    }

    private void dispose() {
        SystemManager.dispose();
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean p) {
        this.pause = p;
    }

    public void setExit(boolean e) {
        this.exit = e;
    }

    public int getDebugLevel() {
        return debug;
    }

    public void setDebugLevel(int d) {
        this.debug = d;
    }

    public static void main(String[] args) {
        Game app = new Game(args, "/config.properties");
        app.run();
    }

    public boolean isDebugAtLeast(int level) {
        return this.debug >= level;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}
