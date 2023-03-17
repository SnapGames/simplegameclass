package fr.snapgames.demo.core;

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
		Dimension playArea;

		public World(double g, Dimension pa) {
			this.gravity = g;
			this.playArea = pa;
		}
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

		Map<String, Animation> animations = new HashMap<>();
		String currentAnimation = "";

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

		public Entity addAnimation(String name, Animation a) {
			this.type = EntityType.IMAGE;
			this.animations.put(name, a);
			if (currentAnimation.equals("")) {
				currentAnimation = name;
			}
			return this;
		}

		public Entity setAnimation(String name) {
			this.currentAnimation = name;
			return this;
		}
	}

	public class Animation {
		BufferedImage[] frames;
		int index = 0;
		boolean loop = true;
		boolean end = false;

		public Animation(BufferedImage[] f) {
			this.frames = f;

		}

		public Animation setLoop(boolean b) {
			this.loop = b;
			return this;
		}

		public BufferedImage getFrame() {
			if (index < frames.length && frames[index] != null) {
				return frames[index];
			} else {
				return null;
			}
		}

		public void next() {
			if (index + 1 < frames.length) {
				index++;
			} else {
				if (loop) {
					index = 0;

				} else {
					index = 0;
					end = true;
				}
			}
		}

		public Animation reset() {
			index = 0;
			return this;
		}
	}

	private Map<String, Object> config;
	private JFrame frame;
	private BufferedImage renderingBuffer;
	private boolean exit;
	private Map<String, Entity> entities = new HashMap<>();
	World world;

	private boolean[] keys = new boolean[65636];
	private int debug;

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

	/**
	 * Convert String key=val to (Type)val
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public Object convert(String key, String val) {
		switch (key) {
			case "game.size", "size", "gs" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.resolution", "resolution", "r" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.physic.gravity", "gravity", "g" -> {
				return Double.valueOf(val);
			}
			case "game.physic.playarea", "gpa" -> {
				String[] value = val.split("x");
				int width = Integer.valueOf(value[0]);
				int height = Integer.valueOf(value[1]);
				Dimension dim = new Dimension(width, height);
				return dim;
			}
			case "game.title", "title", "t" -> {
				return val;
			}
			case "game.debug", "debug", "d" -> {
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

			this.frame = createFrame(
					(String) config.get("game.title"),
					(Dimension) config.get("game.size"),
					(Dimension) config.get("game.resolution"));
			this.debug = (int) config.get("game.debug");
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
		frame.setIconImage(new ImageIcon("/images/sg-logo-image.png").getImage());

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
		world = new World(
				(Double) config.get("game.physic.gravity"),
				(Dimension) config.get("game.physic.playarea"));

		addEntity(new Entity("player",
				(int) ((world.playArea.getWidth() - 8) * 0.5),
				(int) ((world.playArea.getHeight() - 8) * 0.5),
				Color.RED,
				Color.BLACK)
				.setMaterial(new Material("player_mat", 1.0, 0.67, 0.90))
				.addAnimation("walk",
						readAnimation(
								"/images/sprites01.png",
								true,
								new String[] { "0,0,32,32" })));
	}

	private Animation readAnimation(String fileImageSource, boolean loop, String[] framesDef) {
		BufferedImage[] imgs = new BufferedImage[framesDef.length];
		int i = 0;
		try {
			BufferedImage src = ImageIO.read(Main.class.getResourceAsStream(fileImageSource));
			for (String f : framesDef) {
				String[] val = f.split(",");
				int x = Integer.valueOf(val[0]);
				int y = Integer.valueOf(val[1]);
				int w = Integer.valueOf(val[2]);
				int h = Integer.valueOf(val[3]);
				imgs[i++] = src.getSubimage(x, y, w, h);
			}
		} catch (IOException e) {
			System.err.printf("ERROR: unable to read file %s%n", fileImageSource);
		}
		return new Main.Animation(imgs).setLoop(loop);
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
			player.dy += -(8 * step);
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
		Dimension playArea = (Dimension) config.get("game.physic.playarea");
		e.contact = 0;
		if (e.x <= 0) {
			e.x = 0;
			e.dx = -(e.material.elasticity * e.dx);
			e.contact += 1;
		}
		if (e.y <= 0) {
			e.y = 0;
			e.dy = -(e.material.elasticity * e.dy);
			e.contact += 2;
		}
		if (e.x + e.width > playArea.width) {
			e.x = playArea.width - e.width;
			e.dx = -(e.material.elasticity * e.dx);
			e.contact += 4;
		}
		if (e.y + e.height > playArea.height) {
			e.y = playArea.height - e.height;
			e.dy = -(e.material.elasticity * e.dy);
			e.contact += 8;
		}

	}

	private void updateEntity(Entity e) {
		e.dy += world.gravity / e.mass;
		if (e.contact > 0) {
			e.dx *= e.material.friction;
			e.dy *= e.material.friction;
		}
		e.x += e.dx;
		e.y += e.dy;
	}

	private void draw() {
		Dimension playArea = (Dimension) config.get("game.physic.playarea");
		Graphics2D g = (Graphics2D) renderingBuffer.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// clear rendering buffer
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, renderingBuffer.getWidth(), renderingBuffer.getHeight());

		if (this.debug > 0) {
			g.setColor(Color.YELLOW);
			g.drawRect(0, 0, playArea.width, playArea.height);
			g.setColor(Color.gray);
			g.drawRect(10, 10, renderingBuffer.getWidth() - 20, renderingBuffer.getHeight() - 20);
		}
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
					BufferedImage img = e.image;
					if (!e.currentAnimation.equals("")) {
						img = e.animations.get(e.currentAnimation).getFrame();
					}
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
