package fr.snapgames.demo.core.gfx;

import fr.snapgames.demo.core.entity.Camera;
import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.gfx.plugins.DebugGridDrawPlugin;
import fr.snapgames.demo.core.io.UserInput;
import fr.snapgames.demo.core.configuration.ConfigAttribute;
import fr.snapgames.demo.core.entity.Entity;
import fr.snapgames.demo.core.gfx.plugins.EntityDrawPlugin;
import fr.snapgames.demo.core.gfx.plugins.ParticleDrawPlugin;
import fr.snapgames.demo.core.gfx.plugins.TextDrawPlugin;
import fr.snapgames.demo.core.io.resource.ResourceManager;
import fr.snapgames.demo.core.scenes.Scene;
import fr.snapgames.demo.core.system.GameSystem;
import fr.snapgames.demo.core.system.SystemManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Renderer extends GameSystem {
    public static final String NAME = "Renderer";

    private final JFrame frame;
    private Camera camera;
    private BufferedImage renderingBuffer;

    private final Map<Class<? extends Entity>, DrawPlugin<? extends Entity>> plugins = new HashMap<>();
    private Font debugFont;

    private Color clearColor = Color.BLACK;

    public Renderer(Game game) {
        super(game, NAME);
        this.frame = createWindow(
                (String) game.getConfiguration().get(ConfigAttribute.TITLE),
                (Dimension) game.getConfiguration().get(ConfigAttribute.WINDOW_SIZE),
                (Dimension) game.getConfiguration().get(ConfigAttribute.SCREEN_RESOLUTION));
        // initialize font with a default value.
        debugFont = frame.getGraphics().getFont().deriveFont(8.5f);
        // add default DrawPlugin implementations
        addPlugin(new EntityDrawPlugin());
        addPlugin(new TextDrawPlugin());
        addPlugin(new ParticleDrawPlugin());
        addPlugin(new DebugGridDrawPlugin());

    }

    private void addPlugin(DrawPlugin dp) {
        plugins.put(dp.getClassName(), dp);
    }

    private JFrame createWindow(String title, Dimension size, Dimension resolution) {
        ResourceManager rm = (ResourceManager) SystemManager.get(ResourceManager.NAME);

        JFrame frame = new JFrame(title);

        frame.setPreferredSize(size);
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    public void setWindowIcon(BufferedImage iconIMage) {
        if (iconIMage != null && null != this.frame) {
            this.frame.setIconImage(iconIMage);
        }
    }

    public void draw(Scene scene, Map<String, Object> stats) {
        Dimension playArea = (Dimension) getGame().getConfiguration().get(ConfigAttribute.PHYSIC_PLAY_AREA);
        Graphics2D g = renderingBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // clear rendering buffer
        g.setColor(clearColor);
        g.fillRect(0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight());

        // draw something
        scene.getEntities().values().stream()
                .filter(e -> !(e instanceof Camera) && e.isActive())// && camera.isInFOV(e))
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
            scene.getEntities().values().stream()
                    .filter(e -> !(e instanceof Camera) && e.isActive())// && camera.isInFOV(e))
                    .sorted((e1, e2) -> e1.priority > e2.priority ? 1 : -1)
                    .forEach(e -> {
                        if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                            camera.preDraw(g);
                        }
                        drawDebugEntity(g, e);
                        if (Optional.ofNullable(camera).isPresent() && !e.isFixedToCamera()) {
                            camera.postDraw(g);
                        }
                    });
            drawDebugInfoOnScreen(playArea, g);
        }
        g.dispose();

        // draw buffer to window.
        drawToWindow(stats);
    }

    private void drawDebugEntity(Graphics2D g, Entity e) {
        if (plugins.containsKey(e.getClass())) {
            DrawPlugin dp = plugins.get(e.getClass());
            if (e.rotation != 0) {
                g.rotate(e.rotation, e.position.x + (e.width * 0.6), e.position.y + (e.height * 0.5));
            }
            dp.drawDebug(this, g, e);
            if (e.rotation != 0) {
                g.rotate(-e.rotation, e.position.x + (e.width * 0.6), e.position.y + (e.height * 0.5));
            }
        }
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

        if (Optional.ofNullable(this.camera).isPresent()) {
            this.camera.postDraw(g);
        }
    }

    private void drawDebugLine(Graphics2D g, Map<String, Object> stats) {
        Dimension windowSize = (Dimension) this.getGame().getConfiguration().get(ConfigAttribute.WINDOW_SIZE);
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
            return entry.getKey().substring(entry.getKey().indexOf('_') + 1)
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
        drawBox(g, e, (int) x, (int) y);
        // draw object meta info.
        drawMetaInfo(g, e, x, y);
    }

    /**
     * Update the camera.
     *
     * @param scene   the current Scene to be updated.
     * @param elapsed the elapsed time since previous call.
     */
    public void update(Scene scene, long elapsed) {
        if (Optional.ofNullable(this.camera).isPresent()) {
            this.camera.update(elapsed);
        }
    }

    private void drawEntity(Graphics2D g, Entity e) {
        if (plugins.containsKey(e.getClass())) {
            DrawPlugin dp = plugins.get(e.getClass());
            if (e.rotation != 0) {
                g.rotate(e.rotation, e.position.x + (e.width * 0.6), e.position.y + (e.height * 0.5));
            }
            dp.draw(this, g, e);
            if (e.rotation != 0) {
                g.rotate(-e.rotation, e.position.x + (e.width * 0.6), e.position.y + (e.height * 0.5));
            }
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

    public void setDebugFont(Font font) {
        this.debugFont = font;
    }

    public void drawBox(Graphics2D g, Entity e, int x, int y) {
        g.setColor(Color.ORANGE);
        Stroke b = g.getStroke();
        g.setStroke(new BasicStroke(0.2f));
        g.drawRect(x, y, (int) e.width, (int) e.height);
        g.setStroke(b);
    }

    public void drawMetaInfo(Graphics2D g, Entity e, double x, double y) {
        int offsetX = e.width > 100 ? 2 : 2 + (int) e.width;
        g.setColor(Color.ORANGE);
        g.setFont(debugFont);
        int fh = g.getFontMetrics().getHeight();
        int i = 0;
        for (String info : e.getDebugInfo()) {
            g.drawString(info, (int) (x + offsetX), (int) (y + i - 2));
            i += (fh - 3);
        }
    }
}
