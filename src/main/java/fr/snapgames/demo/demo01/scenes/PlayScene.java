package fr.snapgames.demo.demo01.scenes;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.io.PlayerInputBehavior;
import fr.snapgames.demo.core.behaviors.sfx.RainBehavior;
import fr.snapgames.demo.core.behaviors.sfx.RandomGravitatingBehavior;
import fr.snapgames.demo.core.behaviors.sfx.SnowBehavior;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.entity.*;
import fr.snapgames.demo.core.gfx.Renderer;
import fr.snapgames.demo.core.gfx.animation.Animations;
import fr.snapgames.demo.core.io.DebugSwitcher;
import fr.snapgames.demo.core.io.DefaultGameActionListener;
import fr.snapgames.demo.core.io.WeatherSimSwitcher;
import fr.snapgames.demo.core.math.physic.Material;
import fr.snapgames.demo.core.math.physic.PhysicType;
import fr.snapgames.demo.core.math.physic.World;
import fr.snapgames.demo.core.scenes.AbstractScene;

import java.awt.*;
import java.util.Map;

public class PlayScene extends AbstractScene {

    private Animations animations;

    private Font scoreFont;

    public PlayScene(Game g) {
        super(g);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public void prepare(Game g) {
        // load animations from the description file
        animations = new Animations(g, "/animations.properties");
        scoreFont = resourceManager.getFont("/fonts/Prince Valiant.ttf").deriveFont(24.0f);
    }

    @Override
    public void create(Game g) {

        // initialize PhysicEngine world
        World world = new World(
                (Double) config.get(ConfigAttribute.PHYSIC_GRAVITY),
                (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
        physicEngine.setWorld(world);

        // defined in the Game inheriting class
        Entity background = (Entity) new Entity("backImage")
                .setPosition(0, 0)
                .setPhysicType(PhysicType.STATIC)
                .setImage(resourceManager.getImage("/images/backgrounds/forest.jpg"))
                .setPriority(0);
        add(background);

        // add the main player entity.
        Entity player = new Entity("player",
                (int) ((world.getPlayArea().getWidth() - 8) * 0.5),
                (int) ((world.getPlayArea().getHeight() - 8) * 0.5),
                Color.RED,
                Color.BLACK)
                .setSize(32.0, 32.0)
                .setMass(80.0)
                .setPriority(1)
                .setMaterial(new Material("player_mat", 1.0, 0.67, 0.90))
                .add(new PlayerInputBehavior())
                // add Moving information for PLayerInput Behavior
                .setAttribute("step", 1.2)
                .setAttribute("player_jump", -10.0 * 0.2)
                // define animations for the player Entity.
                .add("player_idle", animations.get("player_idle").setSpeed(0.6))
                .add("player_walk", animations.get("player_walk"))
                .add("player_fall", animations.get("player_fall"))
                .add("player_jump", animations.get("player_jump"));
        add(player);
        // add a spinning crystal
        Entity crystal = new Entity("crystal", 30, 30, Color.RED, Color.YELLOW)
                .setSize(16, 16)
                .add("crystal_spinning", animations.get("crystal_spinning").setSpeed(0.5))
                .setPriority(3)
                .setParentRelative(true)
                .add(new RandomGravitatingBehavior(0, -32, 8));
        player.addChild(crystal);
        add(crystal);

        RainBehavior rb = new RainBehavior(this, world, 20, 100, 20);
        SnowBehavior sb = new SnowBehavior(this, world, 2);
        add("rainBehavior", rb);
        add("snowBehavior", sb);

        // add a new weather animation to simulate rain
        Particle weather = (Particle) new Particle("weather", 0, 0, 2000)
                .setPriority(10)
                .add(sb)
                .add(rb)
                .setActive(true);
        add(weather);

        // add a new weather animation to simulate rain

        Dimension vp = (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION);

        TextEntity score = (TextEntity) new TextEntity(
                "score", (int) (vp.width - 80), (int) (vp.height * 0.25))
                .setText("00000")
                .setFont(scoreFont)
                .setTextColor(Color.WHITE)
                .setShadowWidth(2)
                .setShadowColor(new Color(0.0f, 0.0f, 0.0f, 0.6f))
                .setBorderWidth(2)
                .setBorderColor(new Color(0.6f, 0.6f, 0.6f, 0.6f))
                .setPriority(20)
                .setFixedToCamera(true);
        add(score);

        Camera cam = new Camera("myCam")
                .setTarget(player)
                .setTween(0.04)
                .setViewport(vp);
        add(cam);

        DebugGridEntity grid = (DebugGridEntity) new DebugGridEntity("grid", 32, 32, world)
                .setPriority(19)
                .setBorderColor(new Color(0.8f, 0.4f, 0.1f, 0.6f))
                .setFillColor(new Color(0.0f, 0.5f, 0.9f, 0.6f));
        add(grid);

        // default game action(escape & pause)
        userInput.add(new DefaultGameActionListener(g));
        // switch between weather particle animations
        userInput.add(new WeatherSimSwitcher(g));
        // switch through debug mode levels.
        userInput.add(new DebugSwitcher(g));
    }

    @Override
    public void update(Game g, long elapsed, Map<String, Object> data) {

    }

    @Override
    public void draw(Game g, Renderer r, Map<String, Object> data) {

    }

    @Override
    public void dispose(Game g) {

    }
}
