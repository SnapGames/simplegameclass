package fr.snapgames.demo.demo01;

import fr.snapgames.demo.core.*;
import fr.snapgames.demo.core.behaviors.*;
import fr.snapgames.demo.core.behaviors.io.PlayerInputBehavior;
import fr.snapgames.demo.core.behaviors.sfx.RainBehavior;
import fr.snapgames.demo.core.behaviors.sfx.RandomGravitatingBehavior;
import fr.snapgames.demo.core.behaviors.sfx.SnowBehavior;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.entity.Particle;
import fr.snapgames.demo.core.entity.TextEntity;
import fr.snapgames.demo.core.gfx.animation.Animations;
import fr.snapgames.demo.core.io.DebugSwitcher;
import fr.snapgames.demo.core.io.GameActionListener;
import fr.snapgames.demo.core.io.MeteoSwitcher;
import fr.snapgames.demo.core.math.physic.Material;
import fr.snapgames.demo.core.math.physic.World;

import java.awt.*;
import java.util.Map;

public class Demo01 extends Game {
    public Demo01(String[] args, String pathToConfigPropsFile) {
        super(args, pathToConfigPropsFile);
    }

    @Override
    protected void prepare(Map<String, Object> context) {
        // load animations from the description file
        Animations animations = new Animations(this, "/animations.properties");
        context.put("animations", animations);
    }

    @Override
    protected void create(Map<String, Object> context) {
        // initialize PhysicEngine world
        World world = new World(
                (Double) config.get(ConfigAttribute.PHYSIC_GRAVITY),
                (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
        physicEngine.setWorld(world);

        Animations animations = (Animations) context.get("animations");
        // defined in the Game inheriting class
                /*
                Entity background = (Entity) new Entity("backImage")
                                .setPosition(0, 0)
                                .setPhysicType(PhysicType.STATIC)
                                .setImage(resourceSystem.getImage("/images/backgrounds/forest.jpg"))
                                .setPriority(5);
                add(background);
                */

        // add the main player entity.
        Entity player = new Entity("player",
                (int) ((world.getPlayArea().getWidth() - 8) * 0.5),
                (int) ((world.getPlayArea().getHeight() - 8) * 0.5),
                Color.RED,
                Color.BLACK)
                .setSize(32.0, 32.0)
                .setMass(80.0)
                .setPriority(2)
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
        add("rainBehavior", (Behavior<?>) rb);
        add("snowBehavior", (Behavior<?>) sb);

        // add a new particles animation to simulate rain
        Particle particles = (Particle) new Particle("particles", 0, 0, 2000)
                .setPriority(1)
                .add(sb)
                .add(rb)
                .setActive(true);
        add(particles);

        // add a new particles animation to simulate rain

        Dimension vp = (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION);

        TextEntity score = (TextEntity) new TextEntity("score", vp.width - 80, 30)
                .setText("00000")
                .setFont(renderer.getGraphics().getFont().deriveFont(Font.BOLD, 20.0f))
                .setTextColor(Color.WHITE)
                .setShadowWidth(2)
                .setShadowColor(new Color(0.0f, 0.0f, 0.0f, 0.6f))
                .setBorderWidth(2)
                .setBorderColor(new Color(0.6f, 0.6f, 0.6f, 0.6f))
                .setPriority(10)
                .setFixedToCamera(true);
        add(score);

        Camera cam = new Camera("myCam")
                .setTarget(player)
                .setTween(0.04)
                .setViewport(vp);
        add(cam);

        // default game action(escape & pause)
        userInput.add(new GameActionListener(this));
        // switch between meteo particle animations
        userInput.add(new MeteoSwitcher(this));
        // switch through debug mode levels.
        userInput.add(new DebugSwitcher(this));
    }

    public static void main(String[] args) {
        Demo01 demo01 = new Demo01(args, "/demo01.properties");
        demo01.run();
    }
}
