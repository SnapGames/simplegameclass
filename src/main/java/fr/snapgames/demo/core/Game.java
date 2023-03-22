package fr.snapgames.demo.core;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Main Game class application with all its subclasses, encapsulating services
 * and entities.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Game extends JPanel {

    /**
     * This enumeration define all the possible configuration attributes to be used
     * by the {@link Configuration} class.
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public enum ConfigAttribute {
        TITLE(
                "window title",
                "game.title",
                "title,t",
                "Set the window title",
                "MyTitle",
                v -> v),
        DEBUG(
                "debug level",
                "game.debug",
                "debug,d",
                "Set the debug level",
                0,
                Integer::valueOf),
        SCREEN_RESOLUTION(
                "screen resolution",
                "game.screen.resolution",
                "resolution,r",
                "define the screen resolution (pixel rated !)",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        WINDOW_SIZE(
                "window size",
                "game.window.size",
                "size,s",
                "define the window size",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        PHYSIC_PLAY_AREA(
                "play area used as world limit",
                "game.physic.play.area",
                "playarea,p",
                "define the play area size",
                new Dimension(320, 200),
                ConfigAttribute::toDimension),
        PHYSIC_GRAVITY(
                "gravity used for physic world",
                "game.physic.gravity",
                "gravity,g",
                "define the physic gravity to apply to any entity",
                0.981,
                Double::valueOf);

        private final String name;

        private static Dimension toDimension(String value) {
            String[] interpretedValue = value
                    .split("x");
            return new Dimension(
                    Integer.parseInt(interpretedValue[0]),
                    Integer.parseInt(interpretedValue[1]));
        }

        String configAttributeKey;
        String argName;
        Object defaultValue;

        String description;
        Function<String, Object> attrParser;

        ConfigAttribute(String name, String c, String a, String d, Object v, Function<String, Object> p) {
            this.name = name;
            this.attrParser = p;
            this.description = d;
            this.configAttributeKey = c;
            this.argName = a;
            this.defaultValue = v;
        }

        public String getName() {
            return this.name;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getConfigAttributeKey() {
            return this.configAttributeKey;
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

    /**
     * <p>
     * The {@link Configuration} comopnent help to set attributes and their values
     * as configuration with default values.
     * <p>
     * The set of possible configuartrion is defined in a {@link ConfigAttribute}
     * enumeration.
     *
     * <p>
     * The configruation default values are loaded from a file.
     * <p>
     * Those default attribute values can be overriden through CLI arguments, the
     * resulting configuration is then saved to a backup file
     *
     * @author Frédéric Delorme
     * @see ConfigAttribute
     * @since 1.0.0
     */
    public class Configuration {
        ConfigAttribute[] attributes = ConfigAttribute.values();
        private Map<ConfigAttribute, Object> configurationValues = new ConcurrentHashMap<>();

        /**
         * Initialize the COnfiguration set with the properties file values.
         * It also override those default values with argument (args) from CLI.
         *
         * @param file the properties file to be loaded a default configuration values.
         * @param args the Arguments array from java Command Line Interface.
         */
        public Configuration(String file, String[] args) {

            Arrays.stream(attributes).forEach(ca -> configurationValues.put(ca, ca.getDefaultValue()));
            parseConfigFile(file);
            parseArgs(args);
            save("backup.properties");
        }

        /**
         * Parse the configuration properties file to extract default values.
         *
         * @param configFile the path to the Properties file to be loaded as default
         *                   configuration values.
         */
        public int parseConfigFile(String configFile) {
            int status = 0;
            Properties props = new Properties();
            if (Optional.ofNullable(configFile).isPresent()) {
                // Read default jar embedded file
                loadDefaultConfigFile(configFile, props);

                // Overload it with custom file if exists
                loadCustomFileIfExists(configFile, props);

                // if properties values has been loaded
                if (!props.isEmpty()) {
                    status = extractConfigValuesFromProps(configFile, status, props);
                } else {
                    System.err.printf("ERROR: No file %s has been loaded, error in configuration file.%n",
                            configFile);
                    status = -1;
                }
            } else {
                status = -1;
            }
            return status;
        }

        /**
         * Extract the values from the Properties (props) object to full feed the
         * {@link Configuration#configurationValues} with.
         *
         * @param configFile the configuration properties file path
         * @param status     the previous status
         * @param props      the properties file.
         * @return status = -1 if error or previous status value.
         */
        private int extractConfigValuesFromProps(String configFile, int status, Properties props) {
            for (Map.Entry<Object, Object> prop : props.entrySet()) {
                String[] kv = new String[]{(String) prop.getKey(), (String) prop.getValue()};
                if (ifArgumentFoundSetToValue(kv) == null) {
                    System.err.printf("WARNING: file=%s : Unknown property '%s' with value '%s'%n",
                            configFile,
                            prop.getKey(),
                            prop.getValue());
                    status = -1;
                }
            }
            return status;
        }

        /**
         * Retrieve the configuration from an external custom file (out of the JAR) to
         * override default values.
         *
         * @param pathToExternalConfigFile the path to the external configuration file.
         * @param props                    the Properteis instance to be overriden with
         *                                 file
         *                                 extracted values.
         */
        private void loadCustomFileIfExists(String pathToExternalConfigFile, Properties props) {
            String jarDir = "";
            String externalConfigFile = "";
            FileReader customConfigFile = null;
            try {
                externalConfigFile = getJarRootPath(pathToExternalConfigFile);
                customConfigFile = new FileReader(externalConfigFile);
                props.load(customConfigFile);
                System.out.printf("INFO : file=%s : configuration overloaded with side part configuration file.%n",
                        externalConfigFile);
            } catch (URISyntaxException | IOException e) {
                System.out.printf(
                        "WARNING : Side part configuration file %s not found: %s%n",
                        externalConfigFile,
                        e.getMessage());
            } finally {
                if (Optional.ofNullable(customConfigFile).isPresent()) {
                    try {
                        customConfigFile.close();
                    } catch (IOException e) {
                        System.err.printf(
                                "ERROR : unable to close file %s: %s%n",
                                externalConfigFile,
                                e.getMessage());
                    }
                }
            }
        }

        /**
         * Load the default configuration properties file (configFile).
         *
         * @param configFile the path to the internal (JAR inside) configuration
         *                   properties file.
         * @param props      the Properties instance to be full-feed with.
         */
        private void loadDefaultConfigFile(String configFile, Properties props) {
            try {
                props.load(Configuration.class.getResourceAsStream(configFile));
                System.out.printf("INFO : file=%s : find and parse the JAR embedded configuration file.%n",
                        configFile);
            } catch (IOException e) {
                System.err.printf(
                        "ERROR : file=%s : Unable to find and parse the JAR embedded configuration file : %s%n",
                        configFile,
                        e.getMessage());
            }
        }

        private String getJarRootPath(String configFile) throws URISyntaxException {
            String jarDir;
            String externalConfigFile;
            CodeSource codeSource = Game.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            jarDir = jarFile.getParentFile().getPath();
            externalConfigFile = jarDir + File.separator + "my-"
                    + (configFile.startsWith("/") || configFile.startsWith("\\") ? configFile.substring(1)
                    : configFile);
            return externalConfigFile;
        }

        /**
         * Parse the command line interface arguments to extract possible values.
         * if the first arg is '?' or 'help' or 'h', the help message will be displayed.
         * if some argument is pased after the help resuest, only help on t those
         * arguments will be displayed.
         *
         * @param args array of arguments from the Java command line interface.
         */
        public void parseArgs(String[] args) {
            if (args.length > 0) {
                for (String arg : args) {
                    String[] kv = arg.split("=");
                    if ("?help".contains(arg.toLowerCase())) {
                        displayHelpMessage(args);
                        System.exit(0);
                    }
                    ConfigAttribute ca = ifArgumentFoundSetToValue(kv);
                    if (Optional.ofNullable(ca).isPresent()) {
                        System.out.printf("INFO: configuration set from argument '%s=%s'%n", kv[0], kv[1]);
                    } else {
                        displayHelpMessage(kv[0], kv[1]);
                    }
                }
            }
        }

        /**
         * Display specific Help about one or more arguments.
         *
         * @param args the list of arguments froil CLI. The furst one (args[0]) must be
         *             contained in the '?help' string request.
         */
        private void displayHelpMessage(String[] args) {
            if ("?help".contains(args[0].toLowerCase())) {
                System.err.printf("%n---%nShow Help details about the following arguments:%n---%n");
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    Stream<ConfigAttribute> stream = Arrays.stream(attributes);
                    Optional<ConfigAttribute> findCa = stream.filter(ca -> ca.getArgName().equals(arg)).findFirst();
                    if (findCa.isPresent()) {
                        ConfigAttribute correspondingCA = findCa.get();
                        System.err.printf("-> [%s] : %s (default value is %s)%n",
                                correspondingCA.getArgName(),
                                correspondingCA.getDescription(),
                                write(correspondingCA.getDefaultValue()));
                    } else {
                        System.err.printf("ERROR: the argument %s is unknown",
                                args[i]);
                    }
                }
            }
        }

        /**
         * Display ahelp message regarding ubnkon argument and show default Help.
         *
         * @param unknownAttributeName unkown attribute's name
         * @param attributeValue       value trying to be assigned to this unkown
         *                             argument.
         */
        public void displayHelpMessage(String unknownAttributeName, String attributeValue) {
            System.err.printf("ERROR: Unknown atttribute %s with value %s%n", unknownAttributeName, attributeValue);
            displayHelpMessage();
        }

        /**
         * Display default help message for all {@link ConfigAttribute} arguments.
         */
        public void displayHelpMessage() {
            // retrieve current jar name
            String jarName = new java.io.File(Game.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            // show help about the possubke arguments for the configuration
            System.err.printf("%n---%nHelp about configuration%n  $>java -jar %s [args]%n----%n", jarName);
            Arrays.stream(attributes).forEach(ca -> System.err.printf("-> [%s] : %s (default value is %s)%n",
                    ca.getArgName(),
                    ca.getDescription(),
                    write(ca.getDefaultValue())));
        }

        /**
         * If the kv[] (where kv[0] is key and kv[1] si value) is a known Configuration
         * argument, set the corresponding {@link ConfigAttribute} in the
         * {@link Configuration#configurationValues} map.
         *
         * @param kv kv[0] is key and kv[1] si value
         * @return true if updated or false if unknown.
         */
        public ConfigAttribute ifArgumentFoundSetToValue(String[] kv) {
            boolean found = false;
            for (ConfigAttribute ca : attributes) {
                String[] argumentNames = ca.getArgName().split(",");
                if (Arrays.stream(argumentNames)
                        .filter(kv[0]::equals).findFirst()
                        .orElse(null) != null
                        || ca.getConfigAttributeKey().equals(kv[0])) {
                    Object value = ca.getAttrParser().apply(kv[1]);
                    configurationValues.put(ca, value);
                    System.out.printf("INFO: Set the configuration '%s' to '%s'%n", ca.getName(), value.toString());
                    return ca;
                }
            }
            return null;
        }

        /**
         * Retrieve the value of a specific {@link ConfigAttribute}.
         *
         * @param ca the {@link ConfigAttribute} you want to retrieve the value for.
         * @return the current value of the {@link ConfigAttribute} from the
         * {@link Configuration#configurationValues} map.
         */
        public Object get(ConfigAttribute ca) {
            return configurationValues.get(ca);
        }

        /**
         * Save the current configuration into a backup properties file.
         *
         * @param backupFilePath filename for the backup proprteis file.
         */
        public void save(String backupFilePath) {
            Properties props = new Properties();
            for (Map.Entry<ConfigAttribute, Object> e : configurationValues.entrySet()) {
                props.setProperty(e.getKey().configAttributeKey, write(e.getValue()));

            }
            System.out.printf(
                    "INFO: save the current configuration to %s%n",
                    backupFilePath);
            String backupRootFilePath = "";
            try {
                backupRootFilePath = getJarRootPath(backupFilePath);
                props.store(new FileOutputStream(backupRootFilePath), "backup current values");
            } catch (URISyntaxException | IOException e) {
                System.err.printf(
                        "ERROR: Unable to write current configuration values to %s:%s%n",
                        backupFilePath,
                        e.getMessage());
            }
        }

        /**
         * Write the value object as a formatted string according to its type.
         * <ul>
         * <li><code>{@link Dimension}</code> object are converted to a string
         * <code>[width]x[height]</code></li>
         * <li>other object value are just converted to String using their own
         * <code>.toString()</code> implementation.</li>
         * </ul>
         *
         * @param value the object vamue to be output as string in a a formatted way.
         * @return
         */
        private String write(Object value) {
            String output = "";
            switch (value.getClass().getName()) {
                case "java.awt.Dimension" -> {
                    output = ((Dimension) value).width + "x" + ((Dimension) value).width;
                }
                default -> {
                    output = value.toString();
                }
            }
            return output;
        }
    }

    /**
     * Resource management with a cache principle, avoiding accessing disk when not
     * necessary.
     * It is loadnig-and-caching {@link Font} or {@link BufferedImage} with
     * dedicated getter.
     * access to cached resources is insered through the file path (String).
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public class Resources {

        Map<String, Object> resources;

        public Resources() {
            resources = new ConcurrentHashMap<>();
        }

        /**
         * Retrieve an image from resources cache. if not already exist, load it into
         * cache
         *
         * @param file the file to be loaded as an Image
         * @return the corresponding BufferedImage instance.
         */
        public BufferedImage getImage(String file) {
            BufferedImage img = null;
            if (!resources.containsKey(file)) {
                try {
                    img = ImageIO.read(Game.class.getResourceAsStream(file));
                } catch (Exception e) {
                    System.err.printf("Unable to read the image %s", file);
                }
                resources.putIfAbsent(file, img);
            }

            return (BufferedImage) resources.get(file);
        }

        /**
         * Retrieve a font from resources cache. if not already exist, load it into
         * cache
         *
         * @param file the file to be loaded as an Image
         * @return the corresponding BufferedImage instance.
         */
        public Font getFont(String file) {
            Font font = null;

            if (!resources.containsKey(file)) {
                try {
                    font = getFont(file);

                } catch (Exception e) {
                    System.err.printf("ERROR: unable to find font file %s: %s", file, e.getMessage());
                }
                resources.putIfAbsent(file, font);
            }
            return (Font) resources.get(file);
        }
    }

    /**
     * List of possible Entity type. stadard ones can be RECTANGLE, ELLIPSE or
     * IMAGE.
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public enum EntityType {
        RECTANGLE,
        ELLIPSE,
        IMAGE;
    }

    /**
     * Core Entity for all managed object on screen.
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public class Entity {
        static int index = 0;
        public int priority = 0;
        long id = index++;
        String name = "default_" + id;
        EntityType type = EntityType.RECTANGLE;
        double x = 0, y = 0;
        double dx = 0, dy = 0;
        double width = 16, height = 16;

        Shape bbox;
        double mass = 1.0;
        BufferedImage image = null;
        Color borderColor = Color.WHITE;
        Color fillColor = Color.BLUE;
        int direction = 0;
        Material material = new Material("default", 1.0, 0.60, 0.998);
        int contact;

        boolean relativeToParent = false;

        Entity parent;
        private List<Entity> child = new ArrayList<>();

        Map<String, Animation> animations = new HashMap<>();
        String currentAnimation = "";
        Map<String, Object> attributes = new HashMap<>();

        List<Behavior<Entity>> behaviors = new ArrayList<>();
        private boolean fixedToCamera;
        private boolean active = true;

        private long duration = -1;
        private long live = 0;

        public Entity(String name, int x, int y, Color borderColor, Color fillColor) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.dx = 0;
            this.dy = 0;
            updateBBox();
        }

        private void updateBBox() {
            if (type.equals(EntityType.ELLIPSE)) {
                this.bbox = new Ellipse2D.Double(x, y, width, height);
            } else {
                this.bbox = new Rectangle2D.Double(x, y, width, height);
            }
        }

        public Entity setMaterial(Material mat) {
            this.material = mat;
            return this;
        }

        public Entity setMass(double m) {
            this.mass = m;
            return this;
        }

        public Entity setFixedToCamera(boolean f) {
            this.fixedToCamera = f;
            return this;
        }

        public Entity add(String name, Animation a) {
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

        public List<Entity> getChild() {
            return child;
        }

        public Entity addChild(Entity c) {
            c.parent = this;
            this.child.add(c);
            return this;
        }

        public Entity add(Behavior<Entity> b) {
            this.behaviors.add(b);
            return this;
        }

        public Entity setType(EntityType t) {
            this.type = t;
            return this;
        }

        public Entity setParentRelative(boolean pr) {
            this.relativeToParent = pr;
            return this;
        }

        public Entity setAttribute(String key, Object value) {
            attributes.put(key, value);
            return this;
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

        public Entity setPriority(int p) {
            this.priority = p;
            return this;
        }

        public List<Behavior<Entity>> getBehaviors() {
            return behaviors;
        }

        protected Entity setRelativeToParent(boolean rtp) {
            this.relativeToParent = rtp;
            return this;
        }
    }

    public class TextEntity extends Entity {
        String text = "";
        Color textColor = Color.WHITE;
        Color shadowColor = Color.BLACK;
        Font font;

        public TextEntity(String name, int x, int y) {
            super(name, x, y, null, null);
        }

        public TextEntity setText(String txt) {
            this.text = txt;
            return this;
        }

        public TextEntity setTextColor(Color tc) {
            this.textColor = tc;
            return this;
        }

        public TextEntity setShadowColor(Color sc) {
            this.shadowColor = sc;
            return this;
        }

        public TextEntity setFont(Font f) {
            this.font = f;
            return this;
        }
    }

    public class Particle extends Entity {
        int nbParticles = 0;

        public Particle(String name) {
            super(name, 0, 0, null, null);
            this.nbParticles = -1;
            super.setRelativeToParent(true);
        }

        public Particle(String name, int x, int y, int nbParticles) {
            super(name, x, y, null, null);
            this.nbParticles = nbParticles;
        }

        public void createParticle(Game g, ParticleBehavior pb) {
            if (Optional.ofNullable(pb).isPresent()) {
                pb.create(g, this);
            }
        }
    }

    public class Camera extends Entity {
        Entity target;
        double tween;

        double rotation = 0.0;
        Dimension viewport;

        public Camera(String name) {
            super(name, 0, 0, null, null);
        }

        public Camera setTarget(Entity t) {
            this.target = t;
            return this;
        }

        public Camera setTween(double tw) {
            this.tween = tw;
            return this;
        }

        public Camera setViewport(Dimension vp) {
            this.viewport = vp;
            return this;
        }

        public Camera setRotation(double r) {
            this.rotation = r;
            return this;
        }

        public void preDraw(Graphics2D g) {
            g.translate(-x, -y);
            g.rotate(-rotation);
        }

        public void postDraw(Graphics2D g) {

            g.rotate(rotation);
            g.translate(x, y);
        }

        public void update(long elapsed) {
            this.x += Math
                    .ceil((target.x + (target.width * 0.5) - ((viewport.getWidth()) * 0.5) - this.x)
                            * tween * Math.min(elapsed, 0.8));
            this.y += Math
                    .ceil((target.y + (target.height * 0.5) - ((viewport.getHeight()) * 0.5) - this.y)
                            * tween * Math.min(elapsed, 0.8));
        }
    }

    public class Animation {
        BufferedImage[] frames;
        int index = 0;
        boolean loop = true;
        boolean end = false;

        double speed = 1.0;

        long animationTime = 0;
        private long[] frameTimes;

        public Animation(BufferedImage[] f, long[] frameTimes) {
            this.frames = f;
            this.frameTimes = frameTimes;

        }

        public Animation setLoop(boolean b) {
            this.loop = b;
            return this;
        }

        public Animation setSpeed(double s) {
            this.speed = s;
            return this;
        }

        public BufferedImage getFrame() {
            if (index < frames.length && frames[index] != null) {
                return frames[index];
            } else {
                return null;
            }
        }

        public void update(long elapsed) {
            this.animationTime += (elapsed * speed);
            if (this.animationTime > this.frameTimes[this.index]) {
                this.animationTime = 0;
                if (this.index + 1 < this.frames.length) {
                    this.index++;
                } else {
                    if (this.loop) {
                        this.index = 0;
                    } else {
                        this.end = true;
                    }
                }
            }
        }

        public Animation reset() {
            index = 0;
            return this;
        }
    }

    public class Animations {
        Map<String, Animation> animations = new HashMap<>();

        public Animations(String animationFile) {
            loadFromFile(animationFile);
        }

        public Animations() {
            this("/animations.properties");
        }

        /**
         * Load animation's frames from animations.properties file to create
         * {@link Animation} object.
         * format of each animation in the properties file
         *
         * <pre>
         * animation_code=[path-to-image-file];[loop/noloop];{[x],[y],[w],[h],[time]+}
         * </pre>
         * <p>
         * Each line in the file will contain one animation. the 3 part of the
         * description of an animation if composed of multiple
         * <code>[x],[y],[w],[h],[time]+</code> section, each defining one frame.
         * <ul>
         * <li><code>path-to-image-file</code> path to the image file to extract frames
         * from</li>
         * <li><code>loop/noloop</code> loop => the animation will loop.</li>
         * <li><code>x</code> horizontal position in the image file of this frame</li>
         * <li><code>y</code> vertical position in the image file of this frame</li>
         * <li><code>w</code> width of the frame in the image file</li>
         * <li><code>h</code> height of the frame in the image file</li>
         * <li><code>time</code> time duration for this frame in the animation</li>
         * </ul>
         *
         * @param animationFile
         */
        private void loadFromFile(String animationFile) {
            Properties anims = new Properties();
            try {
                anims.load(this.getClass().getResourceAsStream(animationFile));
                for (Map.Entry<Object, Object> e : anims.entrySet()) {
                    String animName = (String) e.getKey();
                    String animFrames = (String) e.getValue();

                    String[] args = animFrames.split(";");
                    Animation anim = loadAnimation(
                            args[0],
                            args[1].equals("loop"),
                            args[2].substring("{".length(), args[2].length() - "}".length()).split("\\+"));
                    animations.put(animName, anim);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        public Animation loadAnimation(String imageSrcPath, boolean loop, String[] framesDef) {
            BufferedImage[] imgs = new BufferedImage[framesDef.length];
            long[] frameTimes = new long[framesDef.length];
            BufferedImage imageSource = resources.getImage(imageSrcPath);
            int i = 0;
            for (String f : framesDef) {
                String[] val = f.split(",");
                int x = Integer.valueOf(val[0]);
                int y = Integer.valueOf(val[1]);
                int w = Integer.valueOf(val[2]);
                int h = Integer.valueOf(val[3]);
                int frameTime = Integer.valueOf(val[4]);
                imgs[i] = imageSource.getSubimage(x, y, w, h);
                frameTimes[i] = frameTime;
                i++;
            }

            return new Game.Animation(imgs, frameTimes).setLoop(loop);
        }

        public Animation get(String animKey) {
            return animations.get(animKey);
        }
    }

    public class UserInput implements KeyListener {

        private final Game game;
        private boolean[] keys = new boolean[65636];

        public UserInput(Game game) {
            this.game = game;
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
                game.setExit(true);
            }
            if (e.getKeyCode() == KeyEvent.VK_PAUSE
                    || e.getKeyCode() == KeyEvent.VK_P) {
                game.setPause(!game.isPause());
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                game.setDebugLevel(game.getDebugLevel() + 1 < 5 ? game.getDebugLevel() + 1 : 0);
            }

        }

        private boolean getKey(int k) {
            return keys[k];
        }
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

    public interface Behavior<T> {
        public default void input(UserInput ui, T e) {

        }

        public default void update(long elapsed, T e) {

        }

        public default void draw(Graphics2D g, T e) {

        }
    }

    public interface ParticleBehavior extends Behavior<Particle> {
        Particle create(Game m, Particle parent);
    }

    /**
     * <p>
     * A simple home grown {@link PhysicEngine} to process all entties and make the
     * behave near some realistic physic law (adapted and simplified ones.)
     * <p>
     * It it base don the {@link Entity#x},{@link Entity#y} position,
     * {@link Entity#dx},{@link Entity#dy} velocity attribtues and the
     * {@link Entity#mass} from the
     * {@link Entity} class.
     * <p>
     * It is using a {@link World} obnject defining {@link World#playArea} and
     * the internal {@link World#gravity} value.
     * <p>
     * Each Entity has its own {@link Material} characteristics, influencing
     * elasticity, roughness, density.
     *
     * <blockquote><strong>INFO:</strong> the {@link PhysicEngine#TIME_FACTOR} is an
     * internal
     * reduction factor to adapt global speed of all object on screen.</blockquote>
     *
     * @author Frédéric Delorme
     * @see Entity
     * @see Material
     * @see World
     * @since 1.0.0
     */
    public class PhysicEngine {

        static final double TIME_FACTOR = 0.045;
        Game game;
        World world;

        public PhysicEngine(Game game) {
            this.game = game;
        }

        private void update(long elapsed) {
            this.game.entities.values().stream()
                    .filter(e -> !(e instanceof Camera) && e.isActive())
                    .sorted((e1, e2) -> e1.priority < e2.priority ? 1 : -1)
                    .forEach(e -> {
                        updateEntity(e, elapsed);
                        if (!e.isRelativeToParent()) {
                            constraintsEntity(e);
                        }
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

        private void updateEntity(Entity e, long elapsed) {
            double time = elapsed * TIME_FACTOR;
            if (!e.relativeToParent) {
                e.dy += world.gravity * 10.0 / e.mass;
                if (e.contact > 0) {
                    e.dx *= e.material.friction;
                    e.dy *= e.material.friction;
                }
                e.x += e.dx * time;
                e.y += e.dy * time;
            }
            // update animation with next frame (if required)
            if (!e.currentAnimation.isEmpty()) {
                e.animations.get(e.currentAnimation).update(elapsed);
            }
            // process attached behaviors
            if (!e.getBehaviors().isEmpty()) {
                e.getBehaviors().forEach(b -> b.update(elapsed, e));
            }

            e.updateBBox();
        }

        public void setWorld(World world) {
            this.world = world;
        }
    }

    public interface DrawPlugin<T extends Entity> {
        public Class<T> getClassName();

        public default void draw(Renderer r, Graphics2D g, Entity t) {
        }
    }

    public class EntityDrawPlugin implements DrawPlugin<Entity> {
        @Override
        public Class<Entity> getClassName() {
            return Entity.class;
        }

        @Override
        public void draw(Renderer r, Graphics2D g, Entity e) {
            double x = e.x;
            double y = e.y;
            if (e.relativeToParent) {
                x = e.parent.x + e.x;
                y = e.parent.y + e.y;
            }

            if (Optional.ofNullable(r.camera).isPresent() && !e.isFixedToCamera()) {
                r.camera.preDraw(g);
            }

            switch (e.type) {
                // draw a simple rectangle
                case RECTANGLE -> {
                    g.setColor(e.fillColor);
                    g.fillRect((int) x, (int) y, (int) e.width, (int) e.height);
                    g.setColor(e.borderColor);
                    g.drawRect((int) x, (int) y, (int) e.width, (int) e.height);
                }
                // draw an ellipse
                case ELLIPSE -> {
                    g.setColor(e.fillColor);
                    g.fillOval((int) x, (int) y, (int) e.width, (int) e.height);
                    g.setColor(e.borderColor);
                    g.drawOval((int) x, (int) y, (int) e.width, (int) e.height);
                }
                // draw the entity corresponding image or current animation image frame
                case IMAGE -> {
                    BufferedImage img = e.image;
                    if (!e.currentAnimation.equals("")) {
                        img = e.animations.get(e.currentAnimation).getFrame();
                    }
                    if (Optional.ofNullable(img).isPresent()) {
                        if (e.direction >= 0) {
                            g.drawImage(
                                    img,
                                    (int) x, (int) y,
                                    (int) e.width, (int) e.height,
                                    null);
                        } else {
                            g.drawImage(
                                    img,
                                    (int) (x + e.width), (int) y,
                                    (int) -e.width, (int) e.height,
                                    null);
                        }
                    }
                }
                default -> {
                    System.err.printf("ERROR: Unable to draw the entity %s%n", e.name);
                }
            }
            // draw debug info if required
            if (r.isDebugAtLeast(2)) {
                r.drawDebugEntityInfo(g, e);
            }
            if (Optional.ofNullable(r.camera).isPresent() && !e.isFixedToCamera()) {
                r.camera.postDraw(g);
            }
        }
    }

    public class TextDrawPlugin implements DrawPlugin<TextEntity> {

        @Override
        public Class<TextEntity> getClassName() {
            return TextEntity.class;
        }

        @Override
        public void draw(Renderer r, Graphics2D g, Entity e) {

            TextEntity textEntity = (TextEntity) e;

            if (Optional.ofNullable(r.camera).isPresent() && !e.isFixedToCamera()) {
                r.camera.preDraw(g);
            }

            g.setFont(textEntity.font);
            g.setColor(textEntity.shadowColor);
            g.drawString(
                    textEntity.text,
                    (int) textEntity.x + 1, (int) textEntity.y + 1);
            g.setColor(textEntity.textColor);
            g.drawString(
                    textEntity.text,
                    (int) textEntity.x, (int) textEntity.y);

            if (Optional.ofNullable(r.camera).isPresent() && !e.isFixedToCamera()) {
                r.camera.postDraw(g);
            }
        }

    }

    public class Renderer {
        private final Game game;
        private JFrame frame;
        private Camera camera;
        private BufferedImage renderingBuffer;
        private Map<Class<? extends Entity>, DrawPlugin<? extends Entity>> plugins = new HashMap<>();

        public Renderer(Game game) {
            this.game = game;
            this.frame = createWindow(
                    (String) config.get(ConfigAttribute.TITLE),
                    (Dimension) config.get(ConfigAttribute.WINDOW_SIZE),
                    (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION));
            addPlugin(new EntityDrawPlugin());
            addPlugin(new TextDrawPlugin());
        }

        private void addPlugin(DrawPlugin dp) {
            plugins.put(dp.getClassName(), dp);
        }

        private JFrame createWindow(String title, Dimension size, Dimension resolution) {

            JFrame frame = new JFrame(title);

            // setPreferredSize(size);
            this.game.setPreferredSize(size);
            frame.setLayout(new GridLayout());
            frame.add(this.game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(resources.getImage("/images/sg-logo-image.png"));

            frame.pack();
            frame.setVisible(true);
            frame.createBufferStrategy(2);

            renderingBuffer = new BufferedImage(
                    resolution.width,
                    resolution.height,
                    BufferedImage.TYPE_INT_ARGB);

            return frame;
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
                drawDebugInfoOnScreen(playArea, g);
            }
            // draw something
            this.game.entities.values().stream()
                    .filter(e -> !(e instanceof Camera) && e.isActive())
                    .sorted((e1, e2) -> e1.priority > e2.priority ? 1 : -1)
                    .forEach(e -> {
                        drawEntity(g, e);
                    });
            g.dispose();

            // draw buffer to window.
            Graphics2D g2 = (Graphics2D) frame.getBufferStrategy().getDrawGraphics();
            g2.drawImage(
                    renderingBuffer,
                    0, 0, frame.getWidth(), frame.getHeight(),
                    0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight(),
                    null);
            drawDebugLine(g2);
            frame.getBufferStrategy().show();
        }

        private void drawDebugInfoOnScreen(Dimension playArea, Graphics2D g) {
            // draw 'camera' limit axis
            g.setColor(Color.CYAN);
            g.drawRect(10, 10, renderingBuffer.getWidth() - 20, renderingBuffer.getHeight() - 20);
            if (Optional.ofNullable(this.camera).isPresent()) {
                this.camera.preDraw(g);
            }
            // draw play area Limit
            g.setColor(Color.BLUE);
            g.drawRect(0, 0, playArea.width, playArea.height);
            // draw a background grid
            drawGrid(playArea, g, 32, 32);
            if (Optional.ofNullable(this.camera).isPresent()) {
                this.camera.postDraw(g);
            }
        }

        private void drawGrid(Dimension playArea, Graphics2D g, int stepX, int stepY) {
            g.setColor(Color.DARK_GRAY);
            for (int ix = 0; ix < playArea.width; ix += stepX) {
                int width = ix + stepX > playArea.width ? playArea.width - (ix + stepX) : stepX;
                g.drawRect(ix, 0, width, playArea.height);
            }
            for (int iy = 0; iy < playArea.height; iy += stepY) {
                int height = iy + stepY > playArea.height ? playArea.height - (iy + stepY) : stepY;
                g.drawRect(0, iy, playArea.width, height);
            }
            g.setColor(Color.BLUE);
            g.drawRect(0, 0, playArea.width, playArea.height);

        }

        private void drawDebugLine(Graphics2D g) {
            Dimension windowSize = (Dimension) this.game.config.get(ConfigAttribute.WINDOW_SIZE);
            g.setColor(new Color(0.6f, 0.3f, 0.0f, 0.8f));
            g.fillRect(0, frame.getHeight() - 28, frame.getWidth(), 20);
            g.setFont(g.getFont().deriveFont(12.0f));
            g.setColor(Color.WHITE);
            g.drawString(String.format("[ dbg:%d | nb:%d | pause: %s | cam:%s ]",
                            game.getDebugLevel(),
                            game.entities.size(),
                            game.pause ? "on" : "off",
                            camera != null ? camera.name : "none"),
                    12, frame.getHeight() - 14);
        }

        private void drawDebugEntityInfo(Graphics2D g, Entity e) {
            double x = e.x;
            double y = e.y;
            if (e.relativeToParent) {
                x = e.parent.x + e.x;
                y = e.parent.y + e.y;
            }

            // draw box
            g.setColor(Color.ORANGE);
            Stroke b = g.getStroke();
            g.setStroke(new BasicStroke(0.2f));
            g.draw(e.bbox);
            g.setStroke(b);

            // draw id and name
            g.setColor(Color.YELLOW);
            g.setFont(g.getFont().deriveFont(9.0f));
            g.drawString(String.format("#%d:%s", e.id, e.name), (int) x, (int) y - 2);
        }

        public void update(long elapsed) {
            if (Optional.ofNullable(this.camera).isPresent()) {
                this.camera.update(elapsed);
            }
        }

        private boolean isDebugAtLeast(int level) {
            return debug >= level;
        }

        private void drawEntity(Graphics2D g, Entity e) {
            if (plugins.containsKey(e.getClass())) {
                DrawPlugin dp = plugins.get(e.getClass());
                dp.draw(this, g, e);
            }
        }

        public void dispose() {
            frame.dispose();
            renderingBuffer = null;
        }

        private Renderer setCamera(Camera cam) {
            this.camera = cam;
            return this;
        }

        public Renderer setUserInput(UserInput ui) {
            frame.addKeyListener(ui);
            return this;

        }
    }

    public class PlayerInput implements Behavior<Entity> {
        @Override
        public void input(UserInput ui, Entity player) {
            boolean move = false;
            if (player.currentAnimation.equals("player_jump")) {
                player.currentAnimation = "player_idle";
            } else if (player.currentAnimation.equals("player_jump") && player.contact != 0) {
                player.currentAnimation = "player_idle";
            } else {
                player.currentAnimation = "player_idle";
            }
            double step = (double) player.getAttribute("step", 0.2);
            double jump = (double) player.getAttribute("player_jump", -4.0 * 0.2);
            if (ui.getKey(KeyEvent.VK_UP)) {
                player.dy += jump;
                player.currentAnimation = "player_jump";
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_DOWN)) {
                player.dy += step;
                player.currentAnimation = "player_jump";
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_LEFT)) {
                player.dx += -step;
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_RIGHT)) {
                player.dx += step;
                move = true;
            }
            if (!move) {
                player.dx = (player.material.friction * player.dx);
                player.dy = (player.material.friction * player.dy);

                player.currentAnimation = "player_idle";
            } else {
                if (player.dx != 0) {
                    player.currentAnimation = "player_walk";
                }
            }
            player.direction = player.dx >= 0 ? 1 : -1;
        }
    }

    private class RandomGravitingBehavior implements Behavior<Entity> {
        private double speed;
        private double radius1;
        private double y;
        private double x;

        public RandomGravitingBehavior(double x, double y, double radius1) {
            this.x = x;
            this.y = y;
            this.radius1 = radius1;
            this.speed = -0.5 + Math.random();
        }

        @Override
        public void update(long elapsed, Entity e) {
            double life = (Double) e.getAttribute("life", Double.valueOf(Math.PI * 2.0));
            life += 0.05 * speed;
            if (life > Math.PI * 2) {
                life = 0;
            }
            if (life < 0) {
                life = Math.PI * 2.0;
            }
            e.x = x + (Math.cos(life) * radius1);
            e.y = y + (Math.sin(life) * radius1)
                    + (Math.sin(life * radius1 * 0.25) * 8.0)
                    + (Math.sin(life * radius1 * 0.5) * 4.0);
            e.setAttribute("life", life);
        }
    }

    private Configuration config;
    private Resources resources;
    private UserInput userInput;
    private PhysicEngine physicEngine;
    private Renderer renderer;
    private Animations animations;
    private boolean exit;
    private boolean pause;
    private Map<String, Entity> entities = new HashMap<>();
    private int debug;

    public Game(String[] args, String pathToConfigPropsFile) {
        config = new Configuration(pathToConfigPropsFile, args);
        initialize();
    }

    public static void main(String[] args) {
        Game app = new Game(args, "/config.properties");
        app.run();
    }

    public void initialize() {
        resources = new Resources();

        physicEngine = new PhysicEngine(this);
        renderer = new Renderer(this);
        userInput = new UserInput(this);
        renderer.setUserInput(userInput);

        animations = new Animations();

        this.debug = (int) config.get(ConfigAttribute.DEBUG);
    }

    public void run() {
        System.out.printf("Main program started%n");
        create();
        System.out.printf("Scene created%n");
        loop();
        dispose();
        System.out.printf("Main program ended%n");
    }

    private void create() {
        World world = new World(
                (Double) config.get(ConfigAttribute.PHYSIC_GRAVITY),
                (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA));
        physicEngine.setWorld(world);

        // add the main player entity.
        Entity player = new Entity("player",
                (int) ((world.playArea.getWidth() - 8) * 0.5),
                (int) ((world.playArea.getHeight() - 8) * 0.5),
                Color.RED,
                Color.BLACK)
                .setSize(32.0, 32.0)
                .setMass(20.0)
                .setPriority(1)
                .setMaterial(new Material("player_mat", 1.0, 0.67, 0.90))
                .add(new PlayerInput())
                .add("player_idle", animations.get("player_idle").setSpeed(0.6))
                .add("player_walk", animations.get("player_walk"))
                .add("player_fall", animations.get("player_fall"))
                .add("player_jump", animations.get("player_jump"));

        Entity crystal = new Entity("crystal", 0, -28, Color.RED, Color.YELLOW)
                .setSize(16, 16)
                .add("crystal_spinning", animations.get("crystal_spinning").setSpeed(0.5))
                .setParentRelative(true)
                .setPriority(2);
        player.addChild(crystal);
        add(player);
        add(crystal);

        Dimension vp = (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION);

        TextEntity score = (TextEntity) new TextEntity("score", vp.width - 80, 20)
                .setText("00000")
                .setFont(getFont().deriveFont(16.0f))
                .setTextColor(Color.WHITE)
                .setShadowColor(Color.BLACK)
                .setPriority(1)
                .setFixedToCamera(true);
        add(score);

        Camera cam = new Camera("myCam")
                .setTarget(player)
                .setTween(0.04)
                .setViewport(vp);
        add(cam);
    }

    private void loop() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        long elapsed = 0;
        double timeFrame = 1000.0 / 60.0;
        while (!exit) {
            startTime = System.currentTimeMillis();
            elapsed = startTime - endTime;
            input();
            if (!pause) {
                physicEngine.update(elapsed);
                renderer.update(elapsed);
            }
            renderer.draw();
            waitForMs((int) (timeFrame - elapsed));
            endTime = startTime;
        }
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

    private boolean isPause() {
        return pause;
    }

    private void setPause(boolean p) {
        this.pause = p;
    }

    private void setExit(boolean e) {
        this.exit = e;
    }

    private int getDebugLevel() {
        return debug;
    }

    private void setDebugLevel(int d) {
        this.debug = d;
    }

    private void add(Entity entity) {
        if (entity instanceof Camera) {
            renderer.setCamera((Camera) entity);
        }
        entities.put(entity.name, entity);
    }

}
