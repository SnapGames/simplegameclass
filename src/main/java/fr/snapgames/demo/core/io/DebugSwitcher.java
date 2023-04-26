package fr.snapgames.demo.core.io;

import fr.snapgames.demo.core.Game;

import java.awt.event.KeyEvent;

public class DebugSwitcher implements UserActionListener {

    private Game game;

    public DebugSwitcher(Game g) {
        this.game = g;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D) {
            game.setDebugLevel(game.getDebugLevel() + 1 < 5 ? game.getDebugLevel() + 1 : 0);
        }
    }
}
