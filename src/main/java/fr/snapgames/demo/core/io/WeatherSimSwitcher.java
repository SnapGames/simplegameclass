package fr.snapgames.demo.core.io;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.behaviors.sfx.RainBehavior;
import fr.snapgames.demo.core.behaviors.sfx.SnowBehavior;
import fr.snapgames.demo.core.entity.Entity;

import java.awt.event.KeyEvent;

public class WeatherSimSwitcher implements UserActionListener {
    private final Game game;
    private int meteoValue;

    public WeatherSimSwitcher(Game game) {
        this.game = game;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_M) {
            meteoValue = (meteoValue + 1 < 3 ? meteoValue + 1 : 0);
            SnowBehavior snow = (SnowBehavior) game.getCurrentScene().getBehaviors().get("snowBehavior");
            RainBehavior rain = (RainBehavior) game.getCurrentScene().getBehaviors().get("rainBehavior");
            Entity particles = game.getCurrentScene().getEntities().get("particles");
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
