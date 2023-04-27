package fr.snapgames.demo.core.io;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.system.GameSystem;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class UserInput extends GameSystem implements KeyListener {
    public static final String NAME = "UserInput";

    private final boolean[] keys = new boolean[65636];

    private final List<UserActionListener> listeners = new ArrayList<>();
    private boolean ctrlKeyPressed;
    private boolean shiftKeyPressed;
    private boolean altKeyPressed;
    private boolean altGrKeyPressed;

    public UserInput(Game game) {
        super(game, NAME);
    }

    public void add(UserActionListener kl) {
        listeners.add(kl);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        listeners.forEach(k -> k.keyTyped(e));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
        checkMetaKeys(e);
        listeners.forEach(k -> k.keyPressed(e));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        checkMetaKeys(e);
        listeners.forEach(k -> k.keyReleased(e));
    }

    private void checkMetaKeys(KeyEvent e) {
        this.ctrlKeyPressed = e.isControlDown();
        this.shiftKeyPressed = e.isShiftDown();
        this.altKeyPressed = e.isAltDown();
        this.altGrKeyPressed = e.isAltGraphDown();
    }

    public boolean getKey(int k) {
        return keys[k];
    }

    public boolean isShiftPressed() {
        return shiftKeyPressed;
    }

    public boolean isCtrlPressed() {
        return ctrlKeyPressed;
    }

    public boolean isAltPressed() {
        return altKeyPressed;
    }

    public boolean isAltGrPressed() {
        return altGrKeyPressed;
    }
}
