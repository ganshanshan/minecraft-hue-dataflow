package io.springoneplatform.dataflow.map;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Keeps track of living entities positions and draws them using Swing.
 *
 * @author Eric Bottard
 */
public class LivingUpdateListener extends JPanel {

	private Map<Integer, ColoredPath> paths = new ConcurrentHashMap<>();

	private int centerX;

	private int centerZ;

	private int maxLength = 10;

	private float zoom = 4;

	public LivingUpdateListener() {
		setBackground(Color.gray);
		setPreferredSize(new Dimension(600, 600));
	}

	@PostConstruct
	public void start() {
		JFrame frame = new JFrame("Map");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this);

		frame.pack();
		frame.setVisible(true);
	}

	public void onSheep(LivingUpdatePayload payload) {
		paths.computeIfAbsent(payload.getEntity(), id -> new ColoredPath(maxLength))
				.add(payload.getX(), payload.getZ())
				.color(new Color(payload.getRed(), payload.getGreen(), payload.getBlue()));

		repaint();
	}

	public void onPlayer(LivingUpdatePayload payload) {
		this.centerX = payload.getX();
		this.centerZ = payload.getZ();
		repaint();
	}

	private static class ColoredPath implements Iterable<Point> {
		private Color color;

		private List<Point> points = new ArrayList<>();

		private int limit;

		private ColoredPath(int limit) {
			this.limit = limit;
		}

		public ColoredPath add(int x, int y) {
			points.add(new Point(x, y));
			if (points.size() > limit) {
				points.remove(0);
			}
			return this;
		}

		public ColoredPath color(Color color) {
			this.color = color;
			return this;
		}

		@Override
		public Iterator<Point> iterator() {
			return points.iterator();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int[] xs = new int[maxLength];
		int[] ys = new int[maxLength];

		g2.scale(zoom, zoom);
		g2.translate(getWidth()/2/zoom - centerX, getHeight()/2/zoom - centerZ);


		// Sheeps
		g2.setStroke(new BasicStroke(2));
		for (ColoredPath path : paths.values()) {
			g2.setColor(path.color);
			for (int i = 0; i < path.points.size(); i++) {
				xs[i] = path.points.get(i).x;
				ys[i] = path.points.get(i).y;
			}
			g2.drawPolyline(xs, ys, path.points.size());
		}

		// Player
		g2.setStroke(new BasicStroke(1));
		g2.setColor(Color.BLACK);
		g2.drawArc(centerX, centerZ, 5, 5, 0, 360);

	}
}
