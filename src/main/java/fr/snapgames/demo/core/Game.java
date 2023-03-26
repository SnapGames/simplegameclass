package fr.snapgames.demo.core;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
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
     * The {@link Configuration} comopnent help to set attributes and their values
     * as configuration with default values.
     * <p>
     * The set of possible configuartrion is defined in a {@link ConfigAttribute}
     * enumeration.
     *
     * <p>
     * The {@link Configuration} default values are loaded from a file.
     * <p>
     * Those default attribute values can be override through CLI arguments, the
     * resulting configuration is then saved to a backup file
     *
     * @author Frédéric Delorme
     * @see ConfigAttribute
     * @since 1.0.0
     */
    public static class Configuration {
        ConfigAttribute[] attributes = ConfigAttribute.values();
        private Map<ConfigAttribute, Object> configurationValues = new ConcurrentHashMap<>();

        /**
         * Initialize the {@link Configuration} set with the properties file values.
         * It also overrides those default values with argument (args) from CLI.
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

        public Configuration() {
            this("/config.properties", new String[]{});
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
        NONE,
        DOT,
        LINE,
        RECTANGLE,
        ELLIPSE,
        IMAGE;
    }

    /**
     * A Node interface to define child/prent hierarchy structure.
     * <p>
     * Here is an implementation with T=Entity
     * <pre>
     *     Object&lt;Entity&gt;
     *     |__ Child1&lt;Entity&gt;
     *     |__ Child2&lt;tEntity&gt;
     * </pre>
     *
     * @param <T> the object type to be hierarchically organized.
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public interface Node<T> {

        String getName();

        long getId();

        T setParent(T p);

        T getParent();

        T addChild(T c);

        List<T> getChild();
    }

    public abstract class AbstractEntity<T extends Node<T>> implements Node<T> {
        private static long index = 0;
        private long id = ++index;
        private String name = "default_" + id;
        protected int priority = 0;
        EntityType type = EntityType.RECTANGLE;

        Vector2D position;

        Vector2D velocity;
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

        T parent;
        private List<T> child = new ArrayList<>();

        Map<String, Animation> animations = new HashMap<>();
        String currentAnimation = "";
        Map<String, Object> attributes = new HashMap<>();

        List<Behavior<T>> behaviors = new ArrayList<>();
        private boolean fixedToCamera;
        private boolean active = true;

        private long duration = -1;
        private long live = 0;
        private PhysicType physicType = PhysicType.DYNAMIC;

        public AbstractEntity(String name, int x, int y, Color borderColor, Color fillColor) {
            this.name = name;
            this.position = new Vector2D(x, y);
            this.velocity = new Vector2D(0, 0);
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            updateBBox();
        }

        void updateBBox() {
            if (type.equals(EntityType.ELLIPSE)) {
                this.bbox = new Ellipse2D.Double(position.x, position.y, width, height);
            } else {
                this.bbox = new Rectangle2D.Double(position.x, position.y, width, height);
            }
        }

        public long getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public T setMaterial(Material mat) {
            this.material = mat;
            return (T) this;
        }

        public T setMass(double m) {
            this.mass = m;
            return (T) this;
        }

        public T setFixedToCamera(boolean f) {
            this.fixedToCamera = f;
            return (T) this;
        }

        public T add(String name, Animation a) {
            this.type = EntityType.IMAGE;
            this.animations.put(name, a);
            if (currentAnimation.equals("")) {
                currentAnimation = name;
                this.width = animations.get(currentAnimation).getFrame().getWidth();
                this.height = animations.get(currentAnimation).getFrame().getHeight();
            }
            return (T) this;
        }

        public T setAnimation(String name) {
            this.currentAnimation = name;
            return (T) this;
        }

        public T setSize(double w, double h) {
            this.width = w;
            this.height = h;
            return (T) this;
        }


        public List<T> getChild() {
            return child;
        }

        public T addChild(T c) {
            c.setParent((T) this);
            this.child.add(c);
            return (T) this;
        }

        public T getParent() {
            return (T) parent;
        }

        public T add(Behavior<T> b) {
            this.behaviors.add(b);
            return (T) this;
        }

        public T setType(EntityType t) {
            this.type = t;
            return (T) this;
        }

        public T setParentRelative(boolean pr) {
            this.relativeToParent = pr;
            return (T) this;
        }

        public T setAttribute(String key, Object value) {
            attributes.put(key, value);
            return (T) this;
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

        public T setPriority(int p) {
            this.priority = p;
            return (T) this;
        }

        public List<Behavior<T>> getBehaviors() {
            return behaviors;
        }

        protected T setRelativeToParent(boolean rtp) {
            this.relativeToParent = rtp;
            return (T) this;
        }

        public List<String> getDebugInfo() {
            List<String> info = new ArrayList<>();
            info.add(String.format("#%d:%s", id, name));
            if (isRelativeToParent()) {
                info.add(String.format("offset:%s", position));
            } else {
                info.add(String.format("pos:%s", position));
            }
            info.add(String.format("sz :%3.02f,%3.02f", width, height));
            info.add(String.format("spd:%s", velocity));
            info.add(String.format("anm:%s", currentAnimation));
            info.add(String.format("life:%s", duration > -1 ? live + "/" + duration : "n/a"));
            return info;
        }

        public T setDuration(long d) {
            this.duration = d;
            if (duration > 0) {
                active = true;
            }
            return (T) this;
        }

        public T setPosition(double x, double y) {
            this.position.x = x;
            this.position.y = y;
            return (T) this;
        }

        public void update(long elapsed) {
            if (duration != -1) {
                live -= elapsed;
                if (live < 0) {
                    live = 0;
                    active = false;
                }
            }
            updateBBox();
        }

        public T setFillColor(Color color) {
            this.fillColor = color;
            return (T) this;
        }


        public T setBorderColor(Color bc) {
            this.borderColor = bc;
            return (T) this;
        }

        public T setVelocity(double dx, double dy) {
            this.velocity.x = dx;
            this.velocity.y = dy;
            return (T) this;
        }

        @Override
        public T setParent(T p) {
            this.parent = p;
            return (T) this;
        }

        public PhysicType getPhysicType() {
            return this.physicType;
        }

        public T setPhysicType(PhysicType pt) {
            this.physicType = pt;
            return (T) this;
        }

        public long getCurrentIndex() {
            return index;
        }

        public T setImage(BufferedImage image) {
            this.image = image;
            setType(EntityType.IMAGE);
            setSize(image.getWidth(), image.getHeight());
            updateBBox();
            return (T) this;
        }

        public T setActivate(boolean active) {
            this.active = active;
            return (T) this;
        }
    }


    /**
     * Core Entity for all managed object on screen.
     *
     * @author Frédéric Delorme
     * @since 1.0.0
     */
    public class Entity extends AbstractEntity<Entity> {
        public Entity(String name, int x, int y, Color borderColor, Color fillColor) {
            super(name, x, y, borderColor, fillColor);
        }

        public Entity(String name) {
            super(name, 0, 0, null, null);
        }

        public String toString() {
            return "#" + this.getId() + ":" + this.getName();
        }
    }

    public class TextEntity extends Entity {
        String text = "";
        Color textColor = Color.WHITE;
        Color shadowColor = Color.BLACK;
        int shadowWidth = 0;
        int borderWidth = 0;
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

        public TextEntity setShadowWidth(int sw) {
            this.shadowWidth = sw;
            return this;
        }

        public TextEntity setBorderWidth(int bw) {
            this.borderWidth = bw;
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
            this.type = EntityType.NONE;
            this.setMass(0.0);
            this.nbParticles = nbParticles;
        }

        public void createParticle(Game g, ParticleBehavior pb) {
            if (Optional.ofNullable(pb).isPresent()) {
                addChild(pb.create(this));
            }
        }

        @Override
        public void update(long elapsed) {
            super.update(elapsed);

        }

        public int getNbParticles() {
            return nbParticles;
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
            g.translate(-position.x, -position.y);
            g.rotate(-rotation);
        }

        public void postDraw(Graphics2D g) {

            g.rotate(rotation);
            g.translate(position.x, position.y);
        }

        public void update(long elapsed) {
            this.position.x += Math
                    .ceil((target.position.x + (target.width * 0.5) - ((viewport.getWidth()) * 0.5) - this.position.x)
                            * tween * Math.min(elapsed, 0.8));
            this.position.y += Math
                    .ceil((target.position.y + (target.height * 0.5) - ((viewport.getHeight()) * 0.5) - this.position.y)
                            * tween * Math.min(elapsed, 0.8));
        }

        /**
         * Check if the {@link Entity} e is in the field of view (viewport) of the {@link Camera}.
         *
         * @param e the {@link Entity} to be field of view checked.
         * @return true if {@link Entity} is in the FOV.
         */
        public boolean isInFOV(Entity e) {
            if (e.isFixedToCamera() || e.getPhysicType().equals(PhysicType.STATIC)) {
                return true;
            } else if (e.isRelativeToParent()) {
                return e.position.x + e.parent.position.x >= position.x && e.position.x + e.parent.position.x <= position.x + viewport.width
                        && e.position.y + e.parent.position.y >= position.y && e.position.y + e.parent.position.y <= position.y + viewport.height;
            } else {
                return e.position.x >= position.x && e.position.x <= position.x + viewport.width
                        && e.position.y >= position.y && e.position.y <= position.y + viewport.height;
            }

        }
    }

    public class Influencer extends Entity {

        public Influencer(String name) {
            super(name);
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
            if (e.getKeyCode() == KeyEvent.VK_M) {
                meteoValue = (meteoValue + 1 < 3 ? meteoValue + 1 : 0);
                SnowBehavior sb = (SnowBehavior) behaviors.get("snowBehavior");
                RainBehavior rb = (RainBehavior) behaviors.get("rainBehavior");
                switch (meteoValue) {
                    case 0 -> {
                        sb.stop();
                        rb.stop();
                    }
                    case 1 -> {
                        sb.stop();
                        rb.start();
                    }
                    case 2 -> {
                        sb.start();
                        rb.stop();
                    }
                }
            }

        }

        private boolean getKey(int k) {
            return keys[k];
        }
    }

    public class World {
        double gravity = 0.981;
        Dimension playArea;

        List<Influencer> influencers = new ArrayList<>();

        public World(double g, Dimension pa) {
            this.gravity = g;
            this.playArea = pa;
        }

        public Dimension getPlayArea() {
            return this.playArea;
        }

        public double getGravity() {
            return this.gravity;
        }

        public World add(Influencer i) {
            this.influencers.add(i);
            return this;
        }

        public List<Influencer> getInfluencers() {
            return influencers;
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

    public class Vector2D {
        public double x, y;

        public Vector2D() {
            x = 0.0f;
            y = 0.0f;
        }

        /**
         * @param x
         * @param y
         */
        public Vector2D(double x, double y) {
            super();
            this.x = x;
            this.y = y;
        }

        public Vector2D add(Vector2D v) {
            return new Vector2D(x + v.x, y + v.y);
        }

        public Vector2D substract(Vector2D v1) {
            return new Vector2D(x - v1.x, y - v1.y);
        }

        public Vector2D multiply(double f) {
            return new Vector2D(x * f, y * f);
        }

        public double dot(Vector2D v1) {

            return v1.x * y + v1.y * x;
        }

        public double length() {
            return Math.sqrt(x * x + y * y);
        }

        public double distance(Vector2D v1) {
            return substract(v1).length();
        }

        public Vector2D divide(double f) {
            return new Vector2D(x / f, y / f);
        }

        public Vector2D normalize() {
            return divide(length());
        }

        public Vector2D negate() {
            return new Vector2D(-x, -y);
        }

        public double angle(Vector2D v1) {
            double vDot = this.dot(v1) / (this.length() * v1.length());
            if (vDot < -1.0)
                vDot = -1.0;
            if (vDot > 1.0)
                vDot = 1.0;
            return Math.acos(vDot);

        }

        public Vector2D addAll(List<Vector2D> forces) {
            Vector2D sum = new Vector2D();
            for (Vector2D f : forces) {
                sum = sum.add(f);
            }
            return sum;
        }

        public String toString() {
            return String.format("{x:%04.2f,y:%04.2f}", x, y);
        }

        public Vector2D maximize(double maxAccel) {
            if (Math.abs(x) > maxAccel) {
                x = Math.signum(x) * maxAccel;
            }
            if (Math.abs(y) > maxAccel) {
                y = Math.signum(y) * maxAccel;
            }
            return this;
        }

        public Vector2D maximize(double maxX, double maxY) {
            if (Math.abs(x) > maxX) {
                x = Math.signum(x) * maxX;
            }
            if (Math.abs(y) > maxY) {
                y = Math.signum(y) * maxY;
            }
            return this;
        }

        public Vector2D ceil(double ceilThreshod) {
            x = Math.copySign((Math.abs(x) < ceilThreshod ? 0 : x), x);
            y = Math.copySign((Math.abs(x) < ceilThreshod ? 0 : y), y);
            return this;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }
            Vector2D vo = (Vector2D) o;
            return Objects.equals(x, vo.x) && Objects.equals(y, vo.y);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public interface Behavior<Entity> {
        public default void input(UserInput ui, Entity e) {

        }

        public default void update(long elapsed, Entity e) {

        }

        public default void draw(Graphics2D g, Entity e) {

        }
    }

    public interface ParticleBehavior<T extends Entity> extends Behavior<T> {
        Particle create(T parent);

        void start();

        void stop();
    }


    private class RainBehavior implements ParticleBehavior<Particle> {
        private int batch = 10;
        Dimension playArea;

        boolean run = false;
        List<Particle> drops = new ArrayList<>();

        public RainBehavior(World world, int batch) {
            this.playArea = world.getPlayArea();
            this.batch = batch;
        }

        @Override
        public void update(long elapsed, Particle e) {
            e.setSize(playArea.width, playArea.height);
            e.setPhysicType(PhysicType.STATIC);
            // add drops to the particles system
            double i = 0.0;
            if (run) {
                double maxBatch = ((this.batch * 0.5) + (this.batch * Math.random() * 0.5));
                while (i < maxBatch && drops.size() < e.getNbParticles()) {
                    drops.add(create(e));
                    i += 1.0;
                }
            }

            drops.stream().forEach(p -> {
                if (p.position.y >= playArea.height - 1) {
                    p.setPosition(playArea.width * Math.random(),
                            0);
                }
            });
        }

        @Override
        public Particle create(Particle parent) {
            Particle pChild = (Particle) new Particle(parent.getName() + "_drop_" + parent.getCurrentIndex())
                    .setType(EntityType.LINE)
                    .setPhysicType(PhysicType.DYNAMIC)
                    .setSize(1, 1)
                    .setPosition(
                            playArea.width * Math.random(),
                            0)
                    .setBorderColor(Color.CYAN)
                    .setMass(5000.01)
                    .setVelocity(0.5 - Math.random() * 1.0, Math.random() * 0.009)
                    .setRelativeToParent(false);
            parent.addChild(pChild);
            add(pChild);
            return pChild;
        }

        public void start() {
            this.run = true;
        }

        public void stop() {
            this.run = false;
        }
    }


    private class SnowBehavior implements ParticleBehavior<Particle> {
        private int batch = 10;
        Dimension playArea;
        List<Particle> drops = new ArrayList<>();

        boolean run = false;

        public SnowBehavior(World world, int batch) {
            this.playArea = world.getPlayArea();
            this.batch = batch;
        }

        @Override
        public void update(long elapsed, Particle e) {
            e.setSize(playArea.width, playArea.height);
            e.setPhysicType(PhysicType.STATIC);
            // add drops to the particles system
            double i = 0.0;
            double maxBatch = ((this.batch * 0.5) + (this.batch * Math.random() * 0.5));
            while (i < maxBatch && drops.size() < e.getNbParticles()) {
                drops.add(create(e));
                i += 1.0;
            }

            drops.stream().forEach(p -> {
                if (p.position.y >= playArea.height - 1) {
                    p.setPosition(playArea.width * Math.random(),
                            0);
                }
            });
        }

        @Override
        public Particle create(Particle parent) {
            Particle pChild = (Particle) new Particle(parent.getName() + "_spark_" + parent.getCurrentIndex())
                    .setType(EntityType.DOT)
                    .setPhysicType(PhysicType.DYNAMIC)
                    .setSize(1, 1)
                    .setPosition(
                            playArea.width * Math.random(),
                            0)
                    .setFillColor(Color.WHITE)
                    .setMass(4000.01)
                    .setVelocity(0.8 - (Math.random() * 1.6), Math.random() * 0.0009)
                    .setRelativeToParent(false);
            parent.addChild(pChild);
            add(pChild);
            return pChild;
        }

        @Override
        public void start() {
            this.run = true;
        }

        @Override
        public void stop() {
            this.run = false;
        }
    }

    public enum PhysicType {
        STATIC,
        DYNAMIC
    }

    /**
     * A simple home-grown {@link PhysicEngine} to process all entities and make the
     * behavior near some realistic physic law (adapted and simplified ones.)
     * <p>
     * It is based on the {@link Entity#position} position,
     * {@link Entity#velocity} velocity attributes and the
     * {@link Entity#mass} from the
     * {@link Entity} class.
     * <p>
     * It is using a {@link World} object defining {@link World#playArea} and
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
                        if (!e.isRelativeToParent() && !e.isFixedToCamera()) {
                            constraintsEntity(e);
                        }
                    });
        }

        private void constraintsEntity(Entity e) {
            Dimension playArea = (Dimension) config.get(ConfigAttribute.PHYSIC_PLAY_AREA);
            e.contact = 0;
            if (e.position.x <= 0) {
                e.position.x = 0;
                e.velocity.x = -(e.material.elasticity * e.velocity.x);
                e.contact += 1;
            }
            if (e.position.y <= 0) {
                e.position.y = 0;
                e.velocity.y = -(e.material.elasticity * e.velocity.y);
                e.contact += 2;
            }
            if (e.position.x + e.width > playArea.width) {
                e.position.x = playArea.width - e.width;
                e.velocity.x = -(e.material.elasticity * e.velocity.x);
                e.contact += 4;
            }
            if (e.position.y + e.height > playArea.height) {
                e.position.y = playArea.height - e.height;
                e.velocity.y = -(e.material.elasticity * e.velocity.y);
                e.contact += 8;
            }
        }

        private void updateEntity(Entity e, long elapsed) {
            double time = elapsed * TIME_FACTOR;
            if (!e.isFixedToCamera() && e.getPhysicType() == PhysicType.DYNAMIC) {
                if (!e.relativeToParent) {
                    if (e.mass != 0) {
                        e.velocity.y += world.gravity * 10.0 / e.mass;
                    }
                    if (e.contact > 0) {
                        e.velocity.y *= e.material.friction;
                        e.velocity.y *= e.material.friction;
                    }
                    e.position.x += e.velocity.x * time;
                    e.position.y += e.velocity.y * time;
                }
            }
            // update animation with next frame (if required)
            if (!e.currentAnimation.isEmpty()) {
                e.animations.get(e.currentAnimation).update(elapsed);
            }
            // process attached behaviors
            if (!e.getBehaviors().isEmpty()) {
                e.getBehaviors().forEach(b -> b.update(elapsed, e));
            }

            e.update(elapsed);
        }

        public void setWorld(World world) {
            this.world = world;
        }
    }

    public interface DrawPlugin<T extends Entity> {
        public Class<T> getClassName();

        public default void draw(Renderer r, Graphics2D g, T t) {
        }
    }

    public abstract class DefaultDrawPlugin<T extends Entity> implements DrawPlugin<T> {
        @Override
        public abstract Class<T> getClassName();

        @Override
        public void draw(Renderer r, Graphics2D g, T e) {
            double x = e.position.x;
            double y = e.position.y;
            if (e.relativeToParent) {
                x = e.parent.position.x + e.position.x;
                y = e.parent.position.y + e.position.y;
            }

            switch (e.type) {
                // draw a simple rectangle
                case RECTANGLE -> {
                    g.setColor(e.fillColor);
                    g.fillRect((int) x, (int) y, (int) e.width, (int) e.height);
                    g.setColor(e.borderColor);
                    g.drawRect((int) x, (int) y, (int) e.width, (int) e.height);
                }
                // draw a line
                case LINE -> {
                    g.setColor(e.borderColor);
                    Stroke bs = g.getStroke();
                    g.setStroke(new BasicStroke((float) e.width));
                    g.drawLine((int) x, (int) y, (int) (e.position.x + e.velocity.x), (int) (e.position.y + e.velocity.y));
                    g.setStroke(bs);
                }
                // draw an ellipse
                case ELLIPSE -> {
                    g.setColor(e.fillColor);
                    g.fillOval((int) x, (int) y, (int) e.width, (int) e.height);
                    g.setColor(e.borderColor);
                    g.drawOval((int) x, (int) y, (int) e.width, (int) e.height);
                }
                // draw a Dot (dot is a width x width Ellipse)
                case DOT -> {
                    g.setColor(e.fillColor);
                    g.fillOval((int) x, (int) y, (int) e.width, (int) e.width);
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
                        e.setSize(img.getWidth(), img.getHeight());
                        e.updateBBox();
                    }
                }
                case NONE -> {
                    //Nothing to do
                }
                default -> {
                    System.err.printf("ERROR: Unable to draw the entity %s%n", e.getName());
                }
            }
            // draw debug info if required
            if (r.isDebugAtLeast(2)) {
                r.drawDebugEntityInfo(g, e);
            }
        }
    }

    public class EntityDrawPlugin extends DefaultDrawPlugin<Entity> {

        @Override
        public Class<Entity> getClassName() {
            return Entity.class;
        }
    }

    public class ParticleDrawPlugin extends DefaultDrawPlugin<Particle> {

        @Override
        public Class<Particle> getClassName() {
            return Particle.class;
        }
    }

    public class TextDrawPlugin implements DrawPlugin<TextEntity> {

        @Override
        public Class<TextEntity> getClassName() {
            return TextEntity.class;
        }

        @Override
        public void draw(Renderer r, Graphics2D g, TextEntity textEntity) {

            if (Optional.ofNullable(r.camera).isPresent() && !textEntity.isFixedToCamera()) {
                r.camera.preDraw(g);
            }

            g.setFont(textEntity.font);

            g.setColor(textEntity.borderColor);
            for (int xb = 0; xb < textEntity.borderWidth; xb++) {
                for (int yb = 0; yb < textEntity.borderWidth; yb++) {
                    g.drawString(
                            textEntity.text,
                            (int) textEntity.position.x - xb, (int) textEntity.position.y - yb);
                    g.drawString(
                            textEntity.text,
                            (int) textEntity.position.x + xb, (int) textEntity.position.y - yb);
                    g.drawString(
                            textEntity.text,
                            (int) textEntity.position.x - xb, (int) textEntity.position.y + yb);
                    g.drawString(
                            textEntity.text,
                            (int) textEntity.position.x + xb, (int) textEntity.position.y + yb);
                }
            }

            g.setColor(textEntity.shadowColor);
            for (int sw = 0; sw < textEntity.shadowWidth; sw++) {
                g.drawString(
                        textEntity.text,
                        (int) textEntity.position.x + sw, (int) textEntity.position.y + sw);
            }


            g.setColor(textEntity.textColor);
            g.drawString(
                    textEntity.text,
                    (int) textEntity.position.x, (int) textEntity.position.y);

            if (Optional.ofNullable(r.camera).isPresent() && !textEntity.isFixedToCamera()) {
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

            // add default DrawPlugin implementations
            addPlugin(new EntityDrawPlugin());
            addPlugin(new TextDrawPlugin());
            addPlugin(new ParticleDrawPlugin());

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
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
            frame.createBufferStrategy(2);

            renderingBuffer = new BufferedImage(
                    resolution.width,
                    resolution.height,
                    BufferedImage.TYPE_INT_ARGB);

            return frame;
        }

        private void draw(Map<String, Object> stats) {
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
                    .filter(e -> !(e instanceof Camera) && e.isActive() && camera.isInFOV(e))
                    .sorted((e1, e2) -> e1.priority > e2.priority ? 1 : -1)
                    .forEach(e -> {

                        if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                            camera.preDraw(g);
                        }
                        drawEntity(g, e);

                        if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                            camera.postDraw(g);
                        }
                    });
            g.dispose();

            // draw buffer to window.
            drawToWindow(stats);
        }

        private void drawToWindow(Map<String, Object> stats) {
            Graphics2D g2 = (Graphics2D) frame.getBufferStrategy().getDrawGraphics();
            g2.drawImage(
                    renderingBuffer,
                    0, 0, frame.getWidth(), frame.getHeight(),
                    0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight(),
                    null);
            drawDebugLine(g2, stats);
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

        private void drawDebugLine(Graphics2D g, Map<String, Object> stats) {
            Dimension windowSize = (Dimension) this.game.config.get(ConfigAttribute.WINDOW_SIZE);
            g.setColor(new Color(0.6f, 0.3f, 0.0f, 0.8f));
            g.fillRect(0, frame.getHeight() - 28, frame.getWidth(), 20);
            g.setFont(g.getFont().deriveFont(12.0f));
            g.setColor(Color.WHITE);
            g.drawString(
                    prepareStatsString(stats, "[", "]"),
                    12, frame.getHeight() - 14);
        }

        public String prepareStatsString(Map<String, Object> attributes, String start, String end) {
            return start + attributes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
                String value = "";
                switch (entry.getValue().getClass().getSimpleName()) {
                    case "Double", "double", "Float", "float" -> {
                        value = String.format("%04.2f", entry.getValue());
                    }
                    case "Integer", "int" -> {
                        value = String.format("%5d", entry.getValue());
                    }
                    default -> {
                        value = entry.getValue().toString();
                    }
                }
                return
                        entry.getKey().substring(((String) entry.getKey().toString()).indexOf('_') + 1)
                                + ":"
                                + value;
            }).collect(Collectors.joining(" | ")) + end;
        }

        private void drawDebugEntityInfo(Graphics2D g, Entity e) {
            double x = e.position.x;
            double y = e.position.y;
            if (e.relativeToParent) {
                x = e.parent.position.x + e.position.x;
                y = e.parent.position.y + e.position.y;
            }

            // draw box
            g.setColor(Color.ORANGE);
            Stroke b = g.getStroke();
            g.setStroke(new BasicStroke(0.2f));
            g.drawRect((int) x, (int) y, (int) e.width, (int) e.height);
            g.setStroke(b);

            // draw id and name
            int offsetX = e.width > 100 ? 2 : 2 + (int) e.width;
            g.setColor(Color.ORANGE);
            g.setFont(g.getFont().deriveFont(9.0f));
            int fh = g.getFontMetrics().getHeight();
            int i = 0;
            for (String info : e.getDebugInfo()) {
                g.drawString(info, (int) (x + offsetX), (int) (y + i - 2));
                i += (fh - 3);
            }
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

        public Camera getCamera() {
            return camera;
        }
    }

    public class PlayerInput implements Behavior<Entity> {
        @Override
        public void input(UserInput ui, Entity player) {
            boolean move = false;
            double step = (double) player.getAttribute("step", 0.2);
            double jump = (double) player.getAttribute("player_jump", -4.0 * 0.2);
            if (ui.getKey(KeyEvent.VK_UP)) {
                player.velocity.y += jump;
                player.currentAnimation = "player_jump";
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_DOWN)) {
                player.velocity.y += step;
                player.currentAnimation = "player_walk";
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_LEFT)) {
                player.velocity.x += -step;
                player.currentAnimation = "player_walk";
                move = true;
            }
            if (ui.getKey(KeyEvent.VK_RIGHT)) {
                player.velocity.x += step;
                player.currentAnimation = "player_walk";
                move = true;
            }
            if (!move) {
                player.velocity.x = (player.material.friction * player.velocity.x);
                player.velocity.y = (player.material.friction * player.velocity.y);
                if (player.velocity.y > 0) {
                    player.currentAnimation = "player_fall";
                } else {
                    player.currentAnimation = "player_idle";
                }
            }
            player.direction = player.velocity.x >= 0 ? 1 : -1;
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
            e.position.x = x + (Math.cos(life) * radius1);
            e.position.y = y + (Math.sin(life) * radius1)
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
        // load animations from the description file
        Animations animations = new Animations("/animations.properties");
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
                (int) ((world.playArea.getWidth() - 8) * 0.5),
                (int) ((world.playArea.getHeight() - 8) * 0.5),
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


        RainBehavior rb = new RainBehavior(world, 2);
        add("rainBehavior", (Behavior<?>) rb);
        // add a new particles animation to simulate rain
        Particle rain = (Particle) new Particle("rain", 0, 0, 2000)
                .add((Behavior) rb)
                .setPriority(1)
                .setActivate(false);
        add(rain);

        // add a new particles animation to simulate rain
        SnowBehavior sb = new SnowBehavior(world, 2);
        add("snowBehavior", (Behavior<?>) sb);
        Particle snow = (Particle) new Particle("snow", 0, 0, 4000)
                .add((Behavior) sb)
                .setPriority(1)
                .setActivate(false);
        add(snow);

        Dimension vp = (Dimension) config.get(ConfigAttribute.SCREEN_RESOLUTION);

        TextEntity score = (TextEntity) new TextEntity("score", vp.width - 80, 30)
                .setText("00000")
                .setFont(getFont().deriveFont(Font.BOLD, 20.0f))
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
        stats.put("dbg", getDebugLevel());
        stats.put("obj", entities.size());
        stats.put("cam", renderer.getCamera() != null ? renderer.getCamera().getName() : "none");
        stats.put("fps", fps);
        stats.put("ups", ups);
        stats.put("time", formatTime(internalTime));
        stats.put("meteo", meteoValue);

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
            cameras.put(entity.getName(), (Camera) entity);
        }
        entities.put(entity.getName(), entity);
    }

    private void add(String key, Behavior<?> b) {
        this.behaviors.put(key, b);
    }

    private Behavior<?> get(String key) {
        return this.behaviors.get(key);
    }

}
