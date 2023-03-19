package fr.snapgames.demo.core;

import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Main application
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Main extends JPanel implements KeyListener {


    public enum ConfigAttribute {
        TITLE("game.title",
                "title",
                "MyTitle",
                "Set the window title",
                v -> v),
        DEBUG("game.debug",
                "debug",
                "Set the debug level",
                0,
                Integer::valueOf),
        SCREEN_RESOLUTION(
                "game.screen.resolution",
                "resolution",
                "define the screen resolution (pixel rated !)",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        WINDOW_SIZE(
                "game.window.size",
                "size",
                "define the window size",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        PHYSIC_PLAY_AREA(
                "game.physic.play.area",
                "playarea",
                "define the play area size",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        PHYSIC_GRAVITY(
                "game.physic.gravity",
                "gravity",
                "define the physic gravity to apply to any entity",
                0.981,
                Double::valueOf);

        private static Dimension toDimension(String value) {
            String[] interpretedValue = value
                    .split("x");
            return new Dimension(
                    Integer.valueOf(interpretedValue[0]),
                    Integer.valueOf(interpretedValue[1]));
        }

        String configAttribute;
        String argName;
        Object defaultValue;

        String description;
        Function<String, Object> attrParser;

        ConfigAttribute(String c, String a, String d, Object v, Function<String, Object> p) {
            this.attrParser = p;
            this.description = d;
            this.configAttribute = c;
            this.argName = a;
            this.defaultValue = v;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getConfigAttribute() {
            return this.configAttribute;
        }

        public String getDescription() {
            return this.description;
        }

        public String getArgName() {
            return this.argName;
        }

        public Function<String, Object> getAttrParser() {
            return this.attrParser;
        }
    }

    public class Configuration {
        ConfigAttribute[] attributes = ConfigAttribute.values();
        private Map<ConfigAttribute, Object> configurationValues = new ConcurrentHashMap<>();

        public Configuration(String file, String[] args) {

            Arrays.stream(attributes).forEach(ca -> {
                configurationValues.put(ca, ca.getDefaultValue());
            });
            parseConfigFile(file);
            parseArgs(args);
        }

        public int parseConfigFile(String configFile) {
            int status = 0;
            Properties props = new Properties();
            if (Optional.ofNullable(configFile).isPresent()) {
                try {
                    props.load(Configuration.class.getResourceAsStream(configFile));
                    for (Map.Entry<Object, Object> prop : props.entrySet()) {
                        String[] kv = new String[]{(String) prop.getKey(), (String) prop.getValue()};
                        if (!ifArgumentFoundSetToValue(kv)) {
                            System.err.printf("file=%s : Unknown property %s with value %s%n",
                                    configFile,
                                    prop.getKey(),
                                    prop.getValue());
                            status = -1;
                        } else {
                            System.out.printf("file=%s : set %s to %s%n",
                                    configFile,
                                    prop.getKey().toString(),
                                    prop.getValue().toString());
                        }
                    }

                } catch (IOException e) {
                    System.err.printf("ERROR : file=%s : Unable to find and parse the configuration file : %s%n",
                            configFile,
                            e.getMessage());
                }
            } else {
                status = -1;
            }
            return status;
        }

        public void parseArgs(String[] args) {
            if (args.length > 0) {
                for (String arg : args) {
                    String[] kv = arg.split("=");
                    if (ifArgumentFoundSetToValue(kv)) {
                        System.out.printf("argument: set %s to %s%n", kv[0], kv[1]);
                    } else {
                        displayHelpMessage(kv[0], kv[1]);
                    }
                }
            }
        }

        public void displayHelpMessage(String unknownAttributeName, String attributeValue) {
            displayHelpMessage();
        }

        public void displayHelpMessage() {
            Arrays.stream(attributes).forEach(ca -> {
                System.err.printf("- %s : %s (default value is %s)%n",
                        ca.getArgName(),
                        ca.getDescription(),
                        ca.getDefaultValue().toString());
            });
        }

        public boolean ifArgumentFoundSetToValue(String[] kv) {
            boolean found = false;
            for (ConfigAttribute ca : attributes) {
                if (ca.getArgName().equals(kv[0]) || ca.getConfigAttribute().equals(kv[0])) {
                    configurationValues.put(ca, ca.getAttrParser().apply(kv[1]));
                    found = true;
                    break;
                }
            }
            return found;
        }

        public void setAttributes(ConfigAttribute[] values) {
            attributes = values;
        }

        public Object get(ConfigAttribute ca) {
            return configurationValues.get(ca);
        }
    }

    public class Resources {
        Map<String, Object> resources;

        public Resources() {
            resources = new ConcurrentHashMap<>();
        }

        public BufferedImage getImage(String file) {
            BufferedImage img = null;
            if (!resources.containsKey(file)) {
                try {
                    img = ImageIO.read(Main.class.getResourceAsStream(file));
                } catch (Exception e) {
                    System.err.printf("Unable to read the image %s", file);
                }
                resources.put(file, img);
            }

            return (BufferedImage) resources.get(file);
        }
    }

    public enum EntityType {
        RECTANGLE,
        ELLIPSE,
        IMAGE;
    }

    public class World {
        double gravity = 0.981;
        Dimension playArea;

        public World(double g, Dimension pa) {
            this.gravity = g;
            this.playArea = pa;
        }
    }

    public class Material {

        String name;
        double density;
        double elasticity;
        double friction;

        public Material(String name, double d, double e, double f) {
            this.name = name;
            this.density = d;
            this.elasticity = e;
            this.friction = f;
        }
    }

    public class Entity {
        static int index = 0;
        long id = index++;
        String name = "default_" + id;
        EntityType type = EntityType.RECTANGLE;
        double x = 0, y = 0;
        double dx = 0, dy = 0;
        double width = 16, height = 16;
        double mass = 1.0;
        BufferedImage image = null;
        Color borderColor = Color.WHITE;
        Color fillColor = Color.BLUE;
        int direction = 0;
        Material material = new Material("default", 1.0, 0.60, 0.998);
        int contact;

        Map<String, Animation> animations = new HashMap<>();
        String currentAnimation = "";

        public Entity(String name, int x, int y, Color borderColor, Color fillColor) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.dx = 0;
            this.dy = 0;
        }

        public Entity setMaterial(Material mat) {
            this.material = mat;
            return this;
        }

        public Entity setMass(double m) {
            this.mass = m;
            return this;
        }

        public Entity addAnimation(String name, Animation a) {
            this.type = EntityType.IMAGE;
            this.animations.put(name, a);
            if (currentAnimation.equals("")) {
                currentAnimation = name;
                this.width = animations.get(currentAnimation).getFrame().getWidth();
                this.height = animations.get(currentAnimation).getFrame().getHeight();
            }
            return this;
        }

        public Entity setAnimation(String name) {
            this.currentAnimation = name;
            return this;
        }

        public Entity setSize(double w, double h) {
            this.width = w;
            this.height = h;
            return this;
        }
    }

    public class Animation {
        BufferedImage[] frames;
        int index = 0;
        boolean loop = true;
        boolean end = false;

        public Animation(BufferedImage[] f) {
            this.frames = f;

        }

        public Animation setLoop(boolean b) {
            this.loop = b;
            return this;
        }

        public BufferedImage getFrame() {
            if (index < frames.length && frames[index] != null) {
                return frames[index];
            } else {
                return null;
            }
        }

        public void next() {
            if (index + 1 < frames.length) {
                index++;
            } else {
                if (loop) {
                    index = 0;

                } else {
                    index = 0;
                    end = true;
                }
            }
        }

        public Animation reset() {
            index = 0;
            return this;
        }
    }

    private Configuration config;

    private Resources resources;

    private JFrame frame;
    private BufferedImage renderingBuffer;
    private boolean exit;
    private Map<String, Entity> entities = new HashMap<>();
    World world;

    private boolean[] keys = new boolean[65636];
    private int debug;

    public Main(String[] args, String pathToConfigPropsFile) {
        config = new Configuration(pathToConfigPropsFile, args);
        initialize();
    }

    public static void main(String[] args) {
        Main app = new Main(args, "/config.properties");
        app.run();
    }

    public void initialize() {
        resources = new Resources();
        this.frame = createFrame(
                (String) config.get(ConfigAttribute.TITLE),
                (Dimension) config.get(ConfigAttribute.WINDOW_SIZE),
                (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION));
        this.debug = (int) config.get(ConfigAttribute.DEBUG);
    }

    private JFrame createFrame(String title, Dimension size, Dimension resolution) {

        JFrame frame = new JFrame(title);

        setPreferredSize(size);
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(resources.getImage("/images/sg-logo-image.png"));

        frame.pack();
        frame.setVisible(true);
        frame.createBufferStrategy(2);

        renderingBuffer = new BufferedImage(
                resolution.width,
                resolution.height,
                BufferedImage.TYPE_INT_ARGB);

        frame.addKeyListener(this);

        return frame;
    }

    public void run() {
        System.out.printf("Main program started%n");
        create();
        System.out.printf("Scene created%n");
        loop();
        dispose();
        System.out.printf("Main programm ended%n");
    }

    private void create() {
        world = new World(
                (Double) config.get(ConfigAttribute.PHYSIC_GRAVITY),
                (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA));

        addEntity(new Entity("player",
                (int) ((world.playArea.getWidth() - 8) * 0.5),
                (int) ((world.playArea.getHeight() - 8) * 0.5),
                Color.RED,
                Color.BLACK)
                .setSize(32.0, 32.0)
                .setMaterial(new Material("player_mat", 1.0, 0.67, 0.90))
                .addAnimation("walk",
                        loadAnimation(
                                "/images/sprites01.png",
                                true,
                                new String[]{"0,0,32,32"}))
        );
    }

    private Animation loadAnimation(String imageSrcPath, boolean loop, String[] framesDef) {
        BufferedImage[] imgs = new BufferedImage[framesDef.length];
        BufferedImage imageSource = resources.getImage(imageSrcPath);
        int i = 0;
        for (String f : framesDef) {
            String[] val = f.split(",");
            int x = Integer.valueOf(val[0]);
            int y = Integer.valueOf(val[1]);
            int w = Integer.valueOf(val[2]);
            int h = Integer.valueOf(val[3]);
            imgs[i++] = imageSource.getSubimage(x, y, w, h);
        }

        return new Main.Animation(imgs).setLoop(loop);
    }

    private void addEntity(Entity entity) {
        entities.put(entity.name, entity);
    }

    private void loop() {
        while (!exit) {
            input();
            update();
            draw();
            waitForMs(16);
        }
    }

    private void waitForMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.printf("ERROR: Unable to wait some ms %s%n", e.getMessage());
        }
    }

    private void input() {
        Entity player = entities.get("player");
        boolean move = false;
        double step = 0.2;
        if (getKey(KeyEvent.VK_UP)) {
            player.dy += -(8 * step);
            move = true;
        }
        if (getKey(KeyEvent.VK_DOWN)) {
            player.dy += step;
            move = true;
        }
        if (getKey(KeyEvent.VK_LEFT)) {
            player.dx += -step;
            move = true;
        }
        if (getKey(KeyEvent.VK_RIGHT)) {
            player.dx += step;
            move = true;
        }
        if (!move) {
            player.dx = (player.material.friction * player.dx);
            player.dy = (player.material.friction * player.dy);
        }
        player.direction = player.dx >= 0 ? 1 : -1;

    }

    private void update() {
        entities.values().stream().forEach(e -> {
            updateEntity(e);
            constraintsEntity(e);
        });
    }

    private void constraintsEntity(Entity e) {
        Dimension playArea = (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA);
        e.contact = 0;
        if (e.x <= 0) {
            e.x = 0;
            e.dx = -(e.material.elasticity * e.dx);
            e.contact += 1;
        }
        if (e.y <= 0) {
            e.y = 0;
            e.dy = -(e.material.elasticity * e.dy);
            e.contact += 2;
        }
        if (e.x + e.width > playArea.width) {
            e.x = playArea.width - e.width;
            e.dx = -(e.material.elasticity * e.dx);
            e.contact += 4;
        }
        if (e.y + e.height > playArea.height) {
            e.y = playArea.height - e.height;
            e.dy = -(e.material.elasticity * e.dy);
            e.contact += 8;
        }

    }

    private void updateEntity(Entity e) {
        e.dy += world.gravity / e.mass;
        if (e.contact > 0) {
            e.dx *= e.material.friction;
            e.dy *= e.material.friction;
        }
        e.x += e.dx;
        e.y += e.dy;
    }

    private void draw() {
        Dimension playArea = (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA);
        Graphics2D g = (Graphics2D) renderingBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // clear rendering buffer
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight());

        if (this.isDebugAtLeast(1)) {
            g.setColor(Color.YELLOW);
            g.drawRect(0, 0, playArea.width, playArea.height);
            g.setColor(Color.gray);
            g.drawRect(10, 10, renderingBuffer.getWidth() - 20, renderingBuffer.getHeight() - 20);
        }
        // draw something
        entities.values().forEach(e -> {
            drawEntity(g, e);
            if (isDebugAtLeast(2)) {
                drawDebugEntityInfo(g, e);
            }
        });

        g.dispose();

        // draw buffer to window.
        frame.getBufferStrategy().getDrawGraphics().drawImage(
                renderingBuffer,
                0, 0, frame.getWidth(), frame.getHeight(),
                0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight(),
                null);
        frame.getBufferStrategy().show();

    }

    private void drawDebugEntityInfo(Graphics2D g, Entity e) {
        g.setColor(Color.YELLOW);
        g.setFont(g.getFont().deriveFont(9.0f));
        g.drawRect((int) e.x, (int) e.y, (int) e.width, (int) e.height);
        g.drawString(String.format("#%d:%s", e.id, e.name), (int) e.x, (int) e.y - 2);
    }

    private boolean isDebugAtLeast(int level) {
        return debug >= level;
    }

    private static void drawEntity(Graphics2D g, Entity e) {
        switch (e.type) {
            case RECTANGLE -> {
                g.setColor(e.fillColor);
                g.fillRect((int) e.x, (int) e.y, (int) e.width, (int) e.height);
                g.setColor(e.borderColor);
                g.drawRect((int) e.x, (int) e.y, (int) e.width, (int) e.height);
            }
            case ELLIPSE -> {
                g.setColor(e.fillColor);
                g.fillOval((int) e.x, (int) e.y, (int) e.width, (int) e.height);
                g.setColor(e.borderColor);
                g.drawOval((int) e.x, (int) e.y, (int) e.width, (int) e.height);
            }
            case IMAGE -> {
                BufferedImage img = e.image;
                if (!e.currentAnimation.equals("")) {
                    img = e.animations.get(e.currentAnimation).getFrame();
                }
                if (Optional.ofNullable(img).isPresent()) {
                    if (e.direction >= 0) {
                        g.drawImage(img, (int) e.x, (int) e.y, (int) e.width, (int) e.height, null);
                    } else {
                        g.drawImage(img, (int) (e.x + e.width), (int) e.y, (int) -e.width, (int) e.height,
                                null);
                    }
                }
            }
            default -> {
                System.err.printf("ERROR: Unable to draw the entity %s%n", e.name);
            }
        }
    }

    private void dispose() {
        frame.dispose();
        renderingBuffer = null;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exit = true;
        }
    }

    private boolean getKey(int k) {
        return keys[k];
    }

}
