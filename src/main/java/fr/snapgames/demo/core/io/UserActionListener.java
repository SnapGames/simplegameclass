package fr.snapgames.demo.core.io;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public interface UserActionListener extends KeyListener {
    default void keyTyped(KeyEvent e) {

    }

    default void keyPressed(KeyEvent e) {

    }

    default void keyReleased(KeyEvent e) {

    }
}
