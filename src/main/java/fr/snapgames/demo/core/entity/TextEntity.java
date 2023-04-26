package fr.snapgames.demo.core.entity;

import java.awt.*;

/**
 * The {@link TextEntity} will be sed to display some text on screen.
 * <p>
 * 5 new atribtues are provided:
 *
 * <ul>
 * <li><code>nbParticles</code> the max number of child particles attached to
 * that {@link Particle} entity,</li>
 * <li><code>text</code> the text to be displayed,</li>
 * <li><code>textColor</code> the color to render text</li>
 * <li><code>shadowColor</code> the color for shadowing text</li>
 * <li><code>font</code> the {@link Font} to be used to draw text.</li>
 * </ul>
 * <p>
 * To create a {@link Particle}:
 *
 * <pre>
 * </pre>
 */
public class TextEntity extends Entity {
    public String text = "";
    public Color textColor = Color.WHITE;
    public Color shadowColor = Color.BLACK;
    public int shadowWidth = 0;
    public int borderWidth = 0;
    public Font font;

    public TextEntity(String name, int x, int y) {
        super(name, x, y, null, null);
    }

    public TextEntity setText(String txt) {
        this.text = txt;
        return this;
    }

    public TextEntity setTextColor(Color tc) {
        this.textColor = tc;
        return this;
    }

    public TextEntity setShadowColor(Color sc) {
        this.shadowColor = sc;
        return this;
    }

    public TextEntity setShadowWidth(int sw) {
        this.shadowWidth = sw;
        return this;
    }

    public TextEntity setBorderWidth(int bw) {
        this.borderWidth = bw;
        return this;
    }

    public TextEntity setFont(Font f) {
        this.font = f;
        return this;
    }
}
