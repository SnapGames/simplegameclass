package fr.snapgames.demo.core.configuration;

import fr.snapgames.demo.core.Game;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
public class Configuration {
    ConfigAttribute[] attributes = ConfigAttribute.values();
    private final Map<ConfigAttribute, Object> configurationValues = new ConcurrentHashMap<>();

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
        String jarName = new File(Game.class.getProtectionDomain()
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
        if (value.getClass().getName().equals("java.awt.Dimension")) {
            output = ((Dimension) value).width + "x" + ((Dimension) value).width;
        } else {
            output = value.toString();
        }
        return output;
    }
}
