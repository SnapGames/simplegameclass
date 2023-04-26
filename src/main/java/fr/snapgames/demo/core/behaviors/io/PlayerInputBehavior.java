package fr.snapgames.demo.core.behaviors.io;

import fr.snapgames.demo.core.behaviors.Behavior;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.io.UserInput;

import java.awt.event.KeyEvent;

public class PlayerInputBehavior implements Behavior<Entity> {
    @Override
    public void input(UserInput ui, Entity player) {
        boolean move = false;
        double step = (double) player.getAttribute("step", 0.1);
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
        if (ui.isShiftPressed()) {
            step *= 2.0;
        }
        if (ui.isCtrlPressed()) {
            step *= 4.0;
        }
        if (ui.getKey(KeyEvent.VK_LEFT)) {
            player.velocity.x = -step;
            player.currentAnimation = "player_walk";
            move = true;
        }
        if (ui.getKey(KeyEvent.VK_RIGHT)) {
            player.velocity.x = step;
            player.currentAnimation = "player_walk";
            move = true;
        }
        if (!move) {
            player.velocity.x = (player.getMaterial().friction * player.velocity.x);
            player.velocity.y = (player.getMaterial().friction * player.velocity.y);
            if (player.velocity.y > 0) {
                player.currentAnimation = "player_fall";
            } else {
                player.currentAnimation = "player_idle";
            }
        }
        player.direction = player.velocity.x >= 0 ? 1 : -1;
    }
}
