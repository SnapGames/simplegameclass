package fr.snapgames.demo.demo01;

import fr.snapgames.demo.core.Game;

import java.awt.*;
import java.util.Map;

public class Demo01 extends Game {
    public Demo01(String[] args, String pathToConfigPropsFile) {
        super(args, pathToConfigPropsFile);
    }

    @Override
    protected void prepare(Map<String, Object> context) {
        // load animations from the description file
        Animations animations = new Animations("/animations.properties");
        context.put("animations", animations);
    }

    @Override
    protected void create(Map<String, Object> context) {
        Animations animations = (Animations) context.get("animations");
        // initialize PhysicEngine world
        World world = new World(
                (Double) config.get(ConfigAttribute.PHYSIC_GRAVITY),
                (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
        physicEngine.setWorld(world);

        Entity background = (Entity) new Entity("backImage")
                .setPosition(0, 0)
                .setPhysicType(PhysicType.STATIC)
                .setImage(resources.getImage("/images/backgrounds/forest.jpg"))
                .setPriority(0);
        add(background);

        // add the main player entity.
        Entity player = new Entity("player",
                (int) ((world.getPlayArea().getWidth() - 8) * 0.5),
                (int) ((world.getPlayArea().getHeight() - 8) * 0.5),
                Color.RED,
                Color.BLACK)
                .setSize(32.0, 32.0)
                .setMass(20.0)
                .setPriority(2)
                .setMaterial(new Material("player_mat", 1.0, 0.67, 0.90))
                .add(new PlayerInput())
                .add("player_idle", animations.get("player_idle").setSpeed(0.6))
                .add("player_walk", animations.get("player_walk"))
                .add("player_fall", animations.get("player_fall"))
                .add("player_jump", animations.get("player_jump"));

        // add a spinning crystal
        Entity crystal = new Entity("crystal", 30, 30, Color.RED, Color.YELLOW)
                .setSize(16, 16)
                .add("crystal_spinning", animations.get("crystal_spinning").setSpeed(0.5))
                .setPriority(3)
                .setParentRelative(true)
                .add(new RandomGravitingBehavior(0, -24, 32));
        player.addChild(crystal);
        add(player);
        add(crystal);

        // add a new particles animation to simulate rain
        Particle rain = (Particle) new Particle("rain", 0, 0, 1000)
                .add((Behavior) new RainBehavior(world, 3))
                .setPriority(1);
        add(rain);

        Dimension vp = (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION);

        TextEntity score = (TextEntity) new TextEntity("score", vp.width - 80, 30)
                .setText("00000")
                .setFont(getFont().deriveFont(Font.BOLD, 20.0f))
                .setTextColor(Color.WHITE)
                .setShadowColor(Color.BLACK)
                .setPriority(10)
                .setFixedToCamera(true);
        add(score);

        Camera cam = new Camera("myCam")
                .setTarget(player)
                .setTween(0.04)
                .setViewport(vp);
        add(cam);
    }

    public static void main(String[] args) {
        Demo01 demo01 = new Demo01(args, "/config.properties");
        demo01.run();
    }
}
