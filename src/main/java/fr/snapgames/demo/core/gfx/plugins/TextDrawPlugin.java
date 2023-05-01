package fr.snapgames.demo.core.gfx.plugins;

import fr.snapgames.demo.core.entity.TextEntity;
import fr.snapgames.demo.core.gfx.DrawPlugin;
import fr.snapgames.demo.core.gfx.Renderer;

import java.awt.*;
import java.util.Optional;

public class TextDrawPlugin implements DrawPlugin<TextEntity> {

    @Override
    public Class<TextEntity> getClassName() {
        return TextEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, TextEntity textEntity) {

        if (Optional.ofNullable(r.getCamera()).isPresent() && !textEntity.isFixedToCamera()) {
            r.getCamera().preDraw(g);
        }

        g.setFont(textEntity.font);
        textEntity.width = g.getFontMetrics().stringWidth(textEntity.text);
        textEntity.height = g.getFontMetrics().getHeight();
        g.setColor(textEntity.borderColor);
        for (int xb = 0; xb < textEntity.borderWidth; xb++) {
            for (int yb = 0; yb < textEntity.borderWidth; yb++) {
                g.drawString(
                        textEntity.text,
                        (int) textEntity.position.x - xb, (int) textEntity.position.y - yb);
                g.drawString(
                        textEntity.text,
                        (int) textEntity.position.x + xb, (int) textEntity.position.y - yb);
                g.drawString(
                        textEntity.text,
                        (int) textEntity.position.x - xb, (int) textEntity.position.y + yb);
                g.drawString(
                        textEntity.text,
                        (int) textEntity.position.x + xb, (int) textEntity.position.y + yb);
            }
        }

        g.setColor(textEntity.shadowColor);
        for (int sw = 0; sw < textEntity.shadowWidth; sw++) {
            g.drawString(
                    textEntity.text,
                    (int) textEntity.position.x + sw, (int) textEntity.position.y + sw);
        }

        g.setColor(textEntity.textColor);
        g.drawString(
                textEntity.text,
                (int) textEntity.position.x, (int) textEntity.position.y);

        if (Optional.ofNullable(r.getCamera()).isPresent() && !textEntity.isFixedToCamera()) {
            r.getCamera().postDraw(g);
        }
    }

    @Override
    public void drawDebug(Renderer r, Graphics2D g, TextEntity t) {
        // draw box
        g.setColor(Color.ORANGE);
        Stroke b = g.getStroke();
        g.setStroke(new BasicStroke(0.2f));
        g.drawRect((int) t.position.x, (int) (t.position.y - t.height), (int) t.width, (int) t.height);
        g.setStroke(b);
        r.drawMetaInfo(g, t, t.position.x, t.position.y);
    }
}
