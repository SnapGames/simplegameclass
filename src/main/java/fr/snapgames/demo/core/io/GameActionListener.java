package fr.snapgames.demo.core.io;

import fr.snapgames.demo.core.Game;

import java.awt.event.KeyEvent;

public class GameActionListener implements UserActionListener {

    private Game game;

    public GameActionListener(Game g) {
        this.game = g;
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            game.setExit(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_PAUSE
                || e.getKeyCode() == KeyEvent.VK_P) {
            game.setPause(!game.isPause());
        }
    }
}