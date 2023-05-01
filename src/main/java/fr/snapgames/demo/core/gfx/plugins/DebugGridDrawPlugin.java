package fr.snapgames.demo.core.gfx.plugins;

import fr.snapgames.demo.core.entity.DebugGridEntity;
import fr.snapgames.demo.core.gfx.DrawPlugin;
import fr.snapgames.demo.core.gfx.Renderer;

import java.awt.*;

public class DebugGridDrawPlugin implements DrawPlugin<DebugGridEntity> {
    @Override
    public Class<DebugGridEntity> getClassName() {
        return DebugGridEntity.class;
    }

    @Override
    public void draw(Renderer r, Graphics2D g, DebugGridEntity debugGridEntity) {

    }

    @Override
    public void drawDebug(Renderer r, Graphics2D g, DebugGridEntity debugGridEntity) {
        Dimension playArea = debugGridEntity.world.getPlayArea();
        int stepX = debugGridEntity.stepX;
        int stepY = debugGridEntity.stepY;
        g.setColor(debugGridEntity.borderColor);
        Stroke gb = g.getStroke();
        Stroke ns = new BasicStroke(0.2f);
        for (int ix = 0; ix < playArea.width; ix += stepX) {
            int width = ix + stepX > playArea.width ? playArea.width - (ix + stepX) : stepX;
            g.drawRect(ix, 0, width, playArea.height);
        }
        for (int iy = 0; iy < playArea.height; iy += stepY) {
            int height = iy + stepY > playArea.height ? playArea.height - (iy + stepY) : stepY;
            g.drawRect(0, iy, playArea.width, height);
        }
        g.setColor(debugGridEntity.fillColor);
        g.drawRect(0, 0, playArea.width, playArea.height);
        g.setStroke(gb);
    }
}
