package fr.snapgames.demo.core.system;

import java.util.HashMap;
import java.util.Map;

/**
 * THe {@link SystemManager} insure the {@link GameSystem} management. It is mainly used by the
 * {@link fr.snapgames.demo.core.Game} class itself to instantiate the required services.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class SystemManager {
    private static Map<String, GameSystem> systems = new HashMap<>();

    /**
     * Add a new Service instance to the managed list.
     *
     * @param system the new instance service to be managed.
     */
    public static void add(GameSystem system) {
        systems.put(system.getName(), system);
    }

    /**
     * Retrieve of a service ({@link GameSystem}) on its name.
     *
     * @param systemName the name of the {@link GameSystem} to be retrieved.
     * @param <T>        the type of the {@link GameSystem} to retrieve.
     * @return the instance of the {@link GameSystem} (if exists).
     */
    public static <T extends GameSystem> T get(String systemName) {
        return (T) systems.get(systemName);
    }

    /**
     * Release ALL {@link GameSystem} managed by the {@link SystemManager}.
     */
    public static void dispose() {
        systems.values().stream().forEach(s -> s.dispose());
    }
}
