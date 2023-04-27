package fr.snapgames.demo.core.system;

import fr.snapgames.demo.core.Game;

/**
 * A {@link GameSystem} is a service managed by the Game itself to provide some
 * specific processing.
 *
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class GameSystem {
    private static String name;

    private Game game;

    /**
     * create the new {@link GameSystem}, creating a link with its parent
     * {@link Game} instance, and initializing its
     * internal name, used to retrieve it later through the {@link SystemManager}.
     *
     * @param g    parent {@link Game} instance.
     * @param name the name for this {@link GameSystem} in the
     *             {@link SystemManager}.
     */
    protected GameSystem(Game g, String systemName) {
        this.game = g;
        name = systemName;
        System.out.printf("INFO: GameSystem %s created.%n", this.name);
    }

    /**
     * return the current prent {@link Game} instance.
     *
     * @return the parent {@link Game} instance of this {@link GameSystem}.
     */
    protected Game getGame() {
        return this.game;
    }

    /**
     * A helper method to retrieve debug level from the {@link Game} instance.
     *
     * @param debugLevel the debug level to compare to.
     * @return true if the debug level value is at least debugLevel.
     */
    public boolean isDebugAtLeast(int debugLevel) {
        return getGame().isDebugAtLeast(debugLevel);
    }

    /**
     * return the current assigned name for this {@link GameSystem}.
     *
     * @return the name of this {@link GameSystem} instance.
     */
    public static String getName() {
        return name;
    }

    /**
     * Dispose of this {@link GameSystem}.
     */
    public void dispose() {
        System.out.printf("INFO: GameSystem %s is disposed.", this.name);
    }
}
