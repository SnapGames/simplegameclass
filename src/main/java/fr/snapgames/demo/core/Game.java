package fr.snapgames.demo.core;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.configuration.Configuration;
import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.Renderer;
import fr.snapgames.demo.core.io.resource.ResourceSystem;
import fr.snapgames.demo.core.io.UserInput;
import fr.snapgames.demo.core.math.physic.PhysicEngine;

import java.util.*;
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
    protected ResourceSystem resourceSystem;
    protected UserInput userInput;
    protected PhysicEngine physicEngine;
    protected Renderer renderer;
    private boolean exit;
    private boolean pause;
    private Map<String, Entity> entities = new HashMap<>();

    private Map<String, Camera> cameras = new HashMap<>();

    private Map<String, Behavior<?>> behaviors = new HashMap<>();

    private int debug;
    private int meteoValue = 0;

    public Game(String[] args, String pathToConfigPropsFile) {
        config = new Configuration(pathToConfigPropsFile, args);
        initialize();
    }

    public void initialize() {
        resourceSystem = new ResourceSystem();

        physicEngine = new PhysicEngine(this);
        renderer = new Renderer(this);
        userInput = new UserInput(this);
        renderer.setUserInput(userInput);

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
            input();
            // update all entities
            if (!pause) {
                physicEngine.update(elapsed);
                renderer.update(elapsed);
                updates++;
                internalTimeFrames += elapsed;
            }

            // prepare statistics
            prepareStats(fps, ups, internalTime, stats);

            // render all entities
            renderer.draw(stats);

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
        stats.put("obj", entities.size());
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

    private void input() {
        entities.values().stream()
                .filter(e -> e.isActive() && !e.getBehaviors().isEmpty())
                .forEach(e -> {
                    e.getBehaviors().forEach(b -> b.input(userInput, e));
                });
        // process attached behaviors
    }

    private void dispose() {
        renderer.dispose();
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

    public void add(Entity entity) {
        if (entity instanceof Camera) {
            renderer.setCamera((Camera) entity);
            cameras.put(entity.getName(), (Camera) entity);
        }
        entities.put(entity.getName(), entity);
    }

    public void add(String key, Behavior<?> b) {
        this.behaviors.put(key, b);
    }

    public Behavior<?> get(String key) {
        return this.behaviors.get(key);
    }

    public static void main(String[] args) {
        Game app = new Game(args, "/config.properties");
        app.run();
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public Map<String, Behavior<?>> getBehaviors() {
        return this.behaviors;
    }

    public boolean isDebugAtLeast(int level) {
        return this.debug >= level;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public ResourceSystem getResourceService() {
        return resourceSystem;
    }
}
