package fr.snapgames.demo.core;

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Main application
 * 
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public class Main extends JPanel implements KeyListener {

	public enum EntityType {
		RECTANGLE,
		ELLIPSE,
		IMAGE;
	}

	public class World {
		double gravity = 0.981;
	}

	public class Material {

		String name;
		double density;
		double elasticity;
		double friction;

		public Material(String name, double d, double e, double f) {
			this.name = name;
			this.density = d;
			this.elasticity = e;
			this.friction = f;
		}
	}

	public class Entity {
		static int index = 0;
		long id = index++;
		String name = "default_" + id;
		EntityType type = EntityType.RECTANGLE;
		double x = 0, y = 0;
		double dx = 0, dy = 0;
		double width = 16, height = 16;
		double mass = 1.0;
		BufferedImage image = null;
		Color borderColor = Color.WHITE;
		Color fillColor = Color.BLUE;
		int direction = 0;
		Material material = new Material("default", 1.0, 0.60, 0.998);
		int contact;

		public Entity(String name, int x, int y, Color borderColor, Color fillColor) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.dx = 0;
			this.dy = 0;
		}

		public Entity setMaterial(Material mat) {
			this.material = mat;
			return this;
		}

		public Entity setMass(double m) {
			this.mass = m;
			return this;
		}
	}

	private Map<String, Object> config;
	private JFrame frame;
	private BufferedImage renderingBuffer;
	private boolean exit;
	private Map<String, Entity> entities = new HashMap<>();

	World world = new World();

	private boolean[] keys = new boolean[65636];

	public Main(String[] args, String pathToConfigPropsFile) {
		config = initialize(pathToConfigPropsFile);
		parseArgs(config, args);
	}

	public static void main(String[] args) {
		Main app = new Main(args, "/config.properties");
		app.run();
	}

	private void parseArgs(Map<String, Object> config, String[] args) {
		int i = 0;
		for (String arg : args) {
			System.out.printf("arg[%d]: %s", arg);
			i++;
			String[] keyVal = arg.split("=");
			config.put(keyVal[0], convert(keyVal[0], keyVal[1]));
		}
	}

	public Object convert(String key, String val) {
		switch (key) {
			case "game.size" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.resolution" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.playarea" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.title" -> {
				return val;
			}
			case "game.debug" -> {
				return Integer.valueOf(val);
			}
			default -> {
				return null;
			}
		}
	}

	public Map<String, Object> initialize(String pathToConfigFile) {
		Properties props = new Properties();
		Map<String, Object> config = null;
		try {
			props.load(Main.class.getResourceAsStream(pathToConfigFile));
			Set<Entry<Object, Object>> propSet = props.entrySet();
			config = new HashMap<>();

			for (Entry<Object, Object> e : props.entrySet()) {
				String key = e.getKey().toString();
				String val = e.getValue().toString();
				Object objVal = convert(key, val);
				config.put(key, objVal);
			}

			config.entrySet().stream().forEach(
					e -> System.out.printf("key:%s,value:%s(%s)%n",
							e.getKey(),
							e.getValue().toString(),
							e.getValue().getClass().getSimpleName()));

			frame = createFrame(
					(String) config.get("game.title"),
					(Dimension) config.get("game.size"),
					(Dimension) config.get("game.resolution"));

		} catch (Exception e) {
			System.err.printf("ERROR: Unable to read the configuration file%s: %s%n", pathToConfigFile, e.getMessage());
		}

		return config;
	}

	private JFrame createFrame(String title, Dimension size, Dimension resolution) {

		JFrame frame = new JFrame(title);

		setPreferredSize(size);
		frame.setContentPane(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		frame.setVisible(true);
		frame.createBufferStrategy(2);

		renderingBuffer = new BufferedImage(
				resolution.width,
				resolution.height,
				BufferedImage.TYPE_INT_ARGB);

		frame.addKeyListener(this);

		return frame;
	}

	public void run() {
		System.out.printf("Main programm started%n");
		create();
		System.out.printf("Scene created%n");
		loop();
		dispose();
		System.out.printf("Main programm ended%n");
	}

	private void create() {
		Dimension bufferSize = (Dimension) config.get("game.playarea");
		addEntity(new Entity("player",
				(int) ((bufferSize.getWidth() - 8) * 0.5),
				(int) ((bufferSize.getHeight() - 8) * 0.5),
				Color.RED,
				Color.BLACK)
				.setMaterial(new Material("player_mat", 1.0, 0.67, 0.90)));
	}

	private void addEntity(Entity entity) {
		entities.put(entity.name, entity);
	}

	private void loop() {
		while (!exit) {
			input();
			update();
			draw();
			waitForMs(16);
		}
	}

	private void waitForMs(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			System.err.printf("ERROR: Unable to wait some ms %s%n", e.getMessage());
		}
	}

	private void input() {
		Entity player = entities.get("player");
		boolean move = false;
		double step = 0.2;
		if (getKey(KeyEvent.VK_UP)) {
			player.dy += -(8*step);
			move = true;
		}
		if (getKey(KeyEvent.VK_DOWN)) {
			player.dy += step;
			move = true;
		}
		if (getKey(KeyEvent.VK_LEFT)) {
			player.dx += -step;
			move = true;
		}
		if (getKey(KeyEvent.VK_RIGHT)) {
			player.dx += step;
			move = true;
		}
		if (!move) {
			player.dx = (player.material.friction * player.dx);
			player.dy = (player.material.friction * player.dy);
		}

	}

	private void update() {
		entities.values().stream().forEach(e -> {
			updateEntity(e);
			constraintsEntity(e);
		});
	}

	private void constraintsEntity(Entity e) {
		Dimension playArea = (Dimension) config.get("game.playarea");
		e.contact = 0;
		if (e.x <= 0) {
			e.x = 0;
			e.dx = -(e.material.elasticity * e.dx);
			e.contact +=1;
		}
		if (e.y <= 0) {
			e.y = 0;
			e.dy = -(e.material.elasticity * e.dy);
			e.contact +=2;
		}
		if (e.x + e.width > playArea.width) {
			e.x = playArea.width - e.width;
			e.dx = -(e.material.elasticity * e.dx);
			e.contact +=4;
		}
		if (e.y + e.height > playArea.height) {
			e.y = playArea.height - e.height;
			e.dy = -(e.material.elasticity * e.dy);
			e.contact +=8;
		}

	}

	private void updateEntity(Entity e) {
		e.dy += world.gravity / e.mass;
		if(e.contact>0){
			e.dx*=e.material.friction;
			e.dy*=e.material.friction;
		}
		e.x += e.dx;
		e.y += e.dy;
	}

	private void draw() {

		Graphics2D g = (Graphics2D) renderingBuffer.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// clear rendering buffer
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight());

		// draw something
		entities.values().stream().forEach(e -> {

			switch (e.type) {
				case RECTANGLE -> {
					g.setColor(e.fillColor);
					g.fillRect((int) e.x, (int) e.y, (int) e.width, (int) e.height);
					g.setColor(e.borderColor);
					g.drawRect((int) e.x, (int) e.y, (int) e.width, (int) e.height);
				}
				case ELLIPSE -> {
					g.setColor(e.fillColor);
					g.fillOval((int) e.x, (int) e.y, (int) e.width, (int) e.height);
					g.setColor(e.borderColor);
					g.drawOval((int) e.x, (int) e.y, (int) e.width, (int) e.height);
				}
				case IMAGE -> {
					if (e.image != null) {
						if (e.direction > 0) {
							g.drawImage(e.image, (int) e.x, (int) e.y, null);
						} else {
							g.drawImage(e.image, (int) (e.x + e.width), (int) e.y, (int) -e.width, (int) e.height,
									null);
						}
					}
				}
				default -> {
					System.err.printf("ERROR: Unable to draw the entity %s%n", e.name);
				}
			}

		});

		g.dispose();

		// draw buttfer to window.
		frame.getBufferStrategy().getDrawGraphics().drawImage(
				renderingBuffer,
				0, 0, frame.getWidth(), frame.getHeight(),
				0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight(),
				null);
		frame.getBufferStrategy().show();

	}

	private void dispose() {
		frame.dispose();
		renderingBuffer = null;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			exit = true;
		}
	}

	private boolean getKey(int k) {
		return keys[k];
	}

}
