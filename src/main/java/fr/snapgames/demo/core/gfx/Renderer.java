package fr.snapgames.demo.core.gfx;

import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.io.UserInput;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.plugins.EntityDrawPlugin;
import fr.snapgames.demo.core.gfx.plugins.ParticleDrawPlugin;
import fr.snapgames.demo.core.gfx.plugins.TextDrawPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Renderer {
    private final Game game;
    private JFrame frame;
    private Camera camera;
    private BufferedImage renderingBuffer;
    private Map<Class<? extends Entity>, DrawPlugin<? extends Entity>> plugins = new HashMap<>();

    public Renderer(Game game) {
        this.game = game;
        this.frame = createWindow(
                (String) game.getConfiguration().get(ConfigAttribute.TITLE),
                (Dimension) game.getConfiguration().get(ConfigAttribute.WINDOW_SIZE),
                (Dimension) game.getConfiguration().get(ConfigAttribute.SCREEN_RESOLUTION));

        // add default DrawPlugin implementations
        addPlugin(new EntityDrawPlugin());
        addPlugin(new TextDrawPlugin());
        addPlugin(new ParticleDrawPlugin());

    }

    private void addPlugin(DrawPlugin dp) {
        plugins.put(dp.getClassName(), dp);
    }

    private JFrame createWindow(String title, Dimension size, Dimension resolution) {

        JFrame frame = new JFrame(title);

        // setPreferredSize(size);
        frame.setPreferredSize(size);
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(game.getResourceService().getImage("/images/sg-logo-image.png"));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        frame.createBufferStrategy(2);

        renderingBuffer = new BufferedImage(
                resolution.width,
                resolution.height,
                BufferedImage.TYPE_INT_ARGB);

        return frame;
    }

    public void draw(Map<String, Object> stats) {
        Dimension playArea = (Dimension) game.getConfiguration().get(ConfigAttribute.PHYSIC_PLAY_AREA);
        Graphics2D g = (Graphics2D) renderingBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // clear rendering buffer
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight());

        // draw something
        this.game.getEntities().values().stream()
                .filter(e -> !(e instanceof Camera) && e.isActive() && camera.isInFOV(e))
                .sorted((e1, e2) -> e1.priority > e2.priority ? 1 : -1)
                .forEach(e -> {

                    if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                        camera.preDraw(g);
                    }
                    drawEntity(g, e);

                    if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                        camera.postDraw(g);
                    }
                });

        if (isDebugAtLeast(1)) {
            drawDebugInfoOnScreen(playArea, g);
        }
        g.dispose();

        // draw buffer to window.
        drawToWindow(stats);
    }

    private void drawToWindow(Map<String, Object> stats) {
        Graphics2D g2 = (Graphics2D) frame.getBufferStrategy().getDrawGraphics();
        g2.drawImage(
                renderingBuffer,
                0, 0, frame.getWidth(), frame.getHeight(),
                0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight(),
                null);
        drawDebugLine(g2, stats);
        frame.getBufferStrategy().show();
    }

    private void drawDebugInfoOnScreen(Dimension playArea, Graphics2D g) {
        // draw 'camera' limit axis
        g.setColor(Color.CYAN);
        g.drawRect(10, 10, renderingBuffer.getWidth() - 20, renderingBuffer.getHeight() - 20);
        if (Optional.ofNullable(this.camera).isPresent()) {
            this.camera.preDraw(g);
        }
        // draw play area Limit
        g.setColor(Color.BLUE);
        g.drawRect(0, 0, playArea.width, playArea.height);
        // draw a background grid
        drawGrid(playArea, g, 32, 32);
        if (Optional.ofNullable(this.camera).isPresent()) {
            this.camera.postDraw(g);
        }
    }

    private void drawGrid(Dimension playArea, Graphics2D g, int stepX, int stepY) {
        g.setColor(Color.DARK_GRAY);
        for (int ix = 0; ix < playArea.width; ix += stepX) {
            int width = ix + stepX > playArea.width ? playArea.width - (ix + stepX) : stepX;
            g.drawRect(ix, 0, width, playArea.height);
        }
        for (int iy = 0; iy < playArea.height; iy += stepY) {
            int height = iy + stepY > playArea.height ? playArea.height - (iy + stepY) : stepY;
            g.drawRect(0, iy, playArea.width, height);
        }
        g.setColor(Color.BLUE);
        g.drawRect(0, 0, playArea.width, playArea.height);

    }

    private void drawDebugLine(Graphics2D g, Map<String, Object> stats) {
        Dimension windowSize = (Dimension) this.game.getConfiguration().get(ConfigAttribute.WINDOW_SIZE);
        g.setColor(new Color(0.6f, 0.3f, 0.0f, 0.8f));
        g.fillRect(0, frame.getHeight() - 28, frame.getWidth(), 20);
        g.setFont(g.getFont().deriveFont(12.0f));
        g.setColor(Color.WHITE);
        g.drawString(
                prepareStatsString(stats, "[", "]"),
                12, frame.getHeight() - 14);
    }

    public String prepareStatsString(Map<String, Object> attributes, String start, String end) {
        return start + attributes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> {
            String value = "";
            switch (entry.getValue().getClass().getSimpleName()) {
                case "Double", "double", "Float", "float" -> {
                    value = String.format("%04.2f", entry.getValue());
                }
                case "Integer", "int" -> {
                    value = String.format("%5d", entry.getValue());
                }
                default -> {
                    value = entry.getValue().toString();
                }
            }
            return entry.getKey().substring(((String) entry.getKey().toString()).indexOf('_') + 1)
                    + ":"
                    + value;
        }).collect(Collectors.joining(" | ")) + end;
    }

    public void drawDebugEntityInfo(Graphics2D g, Entity e) {
        double x = e.position.x;
        double y = e.position.y;
        if (e.isRelativeToParent()) {
            x = e.getParent().position.x + e.position.x;
            y = e.getParent().position.y + e.position.y;
        }

        // draw box
        g.setColor(Color.ORANGE);
        Stroke b = g.getStroke();
        g.setStroke(new BasicStroke(0.2f));
        g.drawRect((int) x, (int) y, (int) e.width, (int) e.height);
        g.setStroke(b);

        // draw id and name
        int offsetX = e.width > 100 ? 2 : 2 + (int) e.width;
        g.setColor(Color.ORANGE);
        g.setFont(g.getFont().deriveFont(9.0f));
        int fh = g.getFontMetrics().getHeight();
        int i = 0;
        for (String info : e.getDebugInfo()) {
            g.drawString(info, (int) (x + offsetX), (int) (y + i - 2));
            i += (fh - 3);
        }
    }

    public void update(long elapsed) {
        if (Optional.ofNullable(this.camera).isPresent()) {
            this.camera.update(elapsed);
        }
    }


    private void drawEntity(Graphics2D g, Entity e) {
        if (plugins.containsKey(e.getClass())) {
            DrawPlugin dp = plugins.get(e.getClass());
            dp.draw(this, g, e);
        }
    }

    public void dispose() {
        frame.dispose();
        renderingBuffer = null;
    }

    public Renderer setCamera(Camera cam) {
        this.camera = cam;
        return this;
    }

    public Renderer setUserInput(UserInput ui) {
        frame.addKeyListener(ui);
        return this;

    }

    public Camera getCamera() {
        return camera;
    }

    public Graphics2D getGraphics() {
        return (Graphics2D) this.renderingBuffer.getGraphics();
    }

    public boolean isDebugAtLeast(int i) {
        return game.isDebugAtLeast(i);
    }
}
