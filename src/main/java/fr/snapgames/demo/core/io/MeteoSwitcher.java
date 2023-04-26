package fr.snapgames.demo.core.io;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.sfx.RainBehavior;
import fr.snapgames.demo.core.behaviors.sfx.SnowBehavior;
import fr.snapgames.demo.core.entity.Entity;

import java.awt.event.KeyEvent;

public class MeteoSwitcher implements UserActionListener {
    private final Game game;
    private int meteoValue;

    public MeteoSwitcher(Game game) {
        this.game = game;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_M) {
            meteoValue = (meteoValue + 1 < 3 ? meteoValue + 1 : 0);
            SnowBehavior snow = (SnowBehavior) game.getBehaviors().get("snowBehavior");
            RainBehavior rain = (RainBehavior) game.getBehaviors().get("rainBehavior");
            Entity particles = game.getEntities().get("particles");
            switch (meteoValue) {
                case 0 -> {
                    rain.stop();
                    snow.stop();
                }
                case 1 -> {
                    rain.start();
                    snow.stop();
                }
                case 2 -> {
                    rain.stop();
                    snow.start();
                }
            }
        }
    }
}
