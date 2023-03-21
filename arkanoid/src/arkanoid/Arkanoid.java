/**Nieves Navas Aguilar 1º DAW
*Hecho 1 b), 2 a) y 2 b)*/
/**
 * 
 */
package arkanoid;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

/**
 * 
 * Comentario ejercicio github
 *
 */
public class Arkanoid extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;

	/* CONSTANTS */

	public static final int SCREEN_WIDTH = 800;
	public static final int SCREEN_HEIGHT = 600;

	public static double ball_radius = 10.0;
	// Lo he dejado en estático en vez de en constante para cambiar su velocidad
	public static double ball_velocity = 0.3;

	public static double paddle_width = 60.0;
	public static double paddle_height = 20.0;
	public static final double PADDLE_VELOCITY = 0.6;

	public static final double BLOCK_WIDTH = 60.0;
	public static final double BLOCK_HEIGHT = 20.0;

	public static final int COUNT_BLOCKS_X = 11;
	public static final int COUNT_BLOCKS_Y = 4;

	public static final int PLAYER_LIVES = 5;

	public static final double FT_SLICE = 1.0;
	public static final double FT_STEP = 1.0;

	private static final String FONT = "Courier New";

	/* GAME VARIABLES */

	private boolean tryAgain = false;
	private boolean running = false;

	private Paddle paddle = new Paddle(SCREEN_WIDTH / 2, SCREEN_HEIGHT - 50);
	private Ball ball = new Ball(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
	private List<Brick> bricks = new ArrayList<Arkanoid.Brick>();
	private ScoreBoard scoreboard = new ScoreBoard();

	private double lastFt;
	private double currentSlice;

	// He creado un array y un random para asignar los colores a los ladrillos
	private Color[] colores = { Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE };
	Random azar = new Random();

	// He creado hashmap para usarlo al contar
	private HashMap<Color, Integer> cuentaColores = new HashMap<>();

	abstract class GameObject {
		abstract double left();

		abstract double right();

		abstract double top();

		abstract double bottom();
	}

	class Rectangle extends GameObject {

		double x, y;
		double sizeX;
		double sizeY;

		double left() {
			return x - sizeX / 2.0;
		}

		double right() {
			return x + sizeX / 2.0;
		}

		double top() {
			return y - sizeY / 2.0;
		}

		double bottom() {
			return y + sizeY / 2.0;
		}

	}

	class ScoreBoard {

		// Creo variable fichas porque ahora los ladrillos no sumarán solo 1 punto
		int fichas = 0;
		int lives = PLAYER_LIVES;
		boolean win = false;
		boolean gameOver = false;
		String text = "";
		int score = 0;
		Font font;

		ScoreBoard() {
			font = new Font(FONT, Font.PLAIN, 12);
			text = "Welcome to Arkanoid Java version";
		}

		void increaseScore() {
//He quitado el score y he añadido fichas porque una cosa son fichas y otra la puntuacion
			fichas++;
// Aquí he hecho lo de la velocidad
			if (fichas > 24) {
				ball_velocity = 0.6;
			} else if (fichas > 34) {
				ball_velocity = 1.2;
			}

			if (fichas == (COUNT_BLOCKS_X * COUNT_BLOCKS_Y)) {
				win = true;
				text = "You have won! \nYour score was: " + score + "\n\nPress Enter to restart";
			} else {
				updateScoreboard();
			}
		}

		void die() {
			lives--;
			if (lives == 0) {
				gameOver = true;
				text = "You have lost! \nYour score was: " + score + "\n\nPress Enter to restart";
			} else {
				updateScoreboard();
			}
		}

		void updateScoreboard() {
			text = "Score: " + score + "  Lives: " + lives;
		}

		void draw(Graphics g) {
			if (win || gameOver) {
				font = font.deriveFont(50f);
				FontMetrics fontMetrics = g.getFontMetrics(font);
				g.setColor(Color.WHITE);
				g.setFont(font);
				int titleHeight = fontMetrics.getHeight();
				int lineNumber = 1;
				for (String line : text.split("\n")) {
					int titleLen = fontMetrics.stringWidth(line);
					g.drawString(line, (SCREEN_WIDTH / 2) - (titleLen / 2),
							(SCREEN_HEIGHT / 4) + (titleHeight * lineNumber));
					lineNumber++;

				}
			} else {
				font = font.deriveFont(34f);
				FontMetrics fontMetrics = g.getFontMetrics(font);
				g.setColor(Color.WHITE);
				g.setFont(font);
				int titleLen = fontMetrics.stringWidth(text);
				// Aquí se pone abajo la puntuación
				int titleHeight = fontMetrics.getHeight() + 550;
				g.drawString(text, (SCREEN_WIDTH / 2) - (titleLen / 2), titleHeight + 5);

			}
		}

	}

	class Paddle extends Rectangle {

		double velocity = 0.0;

		public Paddle(double x, double y) {
			this.x = x;
			this.y = y;
			this.sizeX = paddle_width;
			this.sizeY = paddle_height;
		}

		void update() {
			x += velocity * FT_STEP;
		}

		void stopMove() {
			velocity = 0.0;
		}

		void moveLeft() {
			if (left() > 0.0) {
				velocity = -PADDLE_VELOCITY;
			} else {
				velocity = 0.0;
			}
		}

		void moveRight() {
			if (right() < SCREEN_WIDTH) {
				velocity = PADDLE_VELOCITY;
			} else {
				velocity = 0.0;
			}
		}

		void draw(Graphics g) {

			g.setColor(Color.RED);
			g.fillRect((int) (left()), (int) (top()), (int) sizeX, (int) sizeY);
		}

	}

	class Brick extends Rectangle {

		// He creado una variable color para poder hacer su set para pasarlo a draw
		boolean destroyed = false;
		Color color;

		Brick(double x, double y, Color color) {
			this.x = x;
			this.y = y;
			this.sizeX = BLOCK_WIDTH;
			this.sizeY = BLOCK_HEIGHT;
			this.color = color;
		}

		void setColor(Color color) {
			this.color = color;
		}

		Color getColor() {
			return color;
		}

		void draw(Graphics g) {
			g.setColor(color);

			g.fillRect((int) left(), (int) top(), (int) sizeX, (int) sizeY);
		}
	}

	class Ball extends GameObject {

		double x, y;
		double radius = ball_radius;
		double velocityX = ball_velocity;
		double velocityY = ball_velocity;

		Ball(int x, int y) {
			this.x = x;
			this.y = y;
		}

		void draw(Graphics g) {
			g.setColor(Color.RED);
			g.fillOval((int) left(), (int) top(), (int) radius * 2, (int) radius * 2);
		}

		void update(ScoreBoard scoreBoard, Paddle paddle) {
			x += velocityX * FT_STEP;
			y += velocityY * FT_STEP;

			if (left() < 0)
				velocityX = ball_velocity;
			else if (right() > SCREEN_WIDTH)
				velocityX = -ball_velocity;
			if (top() < 0) {
				velocityY = ball_velocity;
			} else if (bottom() > SCREEN_HEIGHT) {
				velocityY = -ball_velocity;
				x = paddle.x;
				y = paddle.y - 50;
				scoreBoard.die();
			}

		}

		double left() {
			return x - radius;
		}

		double right() {
			return x + radius;
		}

		double top() {
			return y - radius;
		}

		double bottom() {
			return y + radius;
		}

	}

	boolean isIntersecting(GameObject mA, GameObject mB) {
		return mA.right() >= mB.left() && mA.left() <= mB.right() && mA.bottom() >= mB.top() && mA.top() <= mB.bottom();
	}

	void testCollision(Paddle mPaddle, Ball mBall) {
		if (!isIntersecting(mPaddle, mBall))
			return;
		mBall.velocityY = -ball_velocity;
		if (mBall.x < mPaddle.x)
			mBall.velocityX = -ball_velocity;
		else
			mBall.velocityX = ball_velocity;

	}

	void testCollision(Brick mBrick, Ball mBall, ScoreBoard scoreboard) {
		if (!isIntersecting(mBrick, mBall))
			return;

		mBrick.destroyed = true;

		scoreboard.increaseScore();

		double overlapLeft = mBall.right() - mBrick.left();
		double overlapRight = mBrick.right() - mBall.left();
		double overlapTop = mBall.bottom() - mBrick.top();
		double overlapBottom = mBrick.bottom() - mBall.top();

		boolean ballFromLeft = overlapLeft < overlapRight;
		boolean ballFromTop = overlapTop < overlapBottom;

		double minOverlapX = ballFromLeft ? overlapLeft : overlapRight;
		double minOverlapY = ballFromTop ? overlapTop : overlapBottom;

		if (minOverlapX < minOverlapY) {
			mBall.velocityX = ballFromLeft ? -ball_velocity : ball_velocity;
		} else {
			mBall.velocityY = ballFromTop ? -ball_velocity : ball_velocity;
		}

		// Aquí se cuentan

		if (mBrick.getColor() == Color.RED) {
			scoreboard.score += cuentaColores.get(Color.RED);
		} else if (mBrick.getColor() == Color.YELLOW) {
			scoreboard.score += cuentaColores.get(Color.YELLOW);
		} else if (mBrick.getColor() == Color.GREEN) {
			scoreboard.score += cuentaColores.get(Color.GREEN);
		} else {
			scoreboard.score += cuentaColores.get(Color.BLUE);
		}
	}

	void initializeBricks(List<Brick> bricks) {
		// deallocate old bricks
		bricks.clear();

		for (int iX = 0; iX < COUNT_BLOCKS_X; ++iX) {
			for (int iY = 0; iY < COUNT_BLOCKS_Y; ++iY) {
				// Aquí le he dado el color aleatorio
				Brick brick = new Brick((iX + 1) * (BLOCK_WIDTH + 3) + 22, (iY + 2) * (BLOCK_HEIGHT + 3) + 20,
						colores[azar.nextInt(4)]);
				bricks.add(brick);
				cuentaColores.put(Color.RED, 1);
				cuentaColores.put(Color.GREEN, 2);
				cuentaColores.put(Color.YELLOW, 5);
				cuentaColores.put(Color.BLUE, 7);
			}
		}
	}

	public Arkanoid() {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setUndecorated(false);
		this.setResizable(false);
		this.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		this.setVisible(true);
		this.addKeyListener(this);
		this.setLocationRelativeTo(null);

		this.createBufferStrategy(2);

		initializeBricks(bricks);

	}

	void run() {

		BufferStrategy bf = this.getBufferStrategy();
		Graphics g = bf.getDrawGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());

		running = true;

		while (running) {

			long time1 = System.currentTimeMillis();

			if (!scoreboard.gameOver && !scoreboard.win) {
				tryAgain = false;
				update();
				drawScene(ball, bricks, scoreboard);

				// to simulate low FPS
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} else {
				if (tryAgain) {
					tryAgain = false;
					initializeBricks(bricks);
					scoreboard.lives = PLAYER_LIVES;
					scoreboard.score = 0;
					scoreboard.win = false;
					scoreboard.gameOver = false;
					scoreboard.updateScoreboard();
					ball.x = SCREEN_WIDTH / 2;
					ball.y = SCREEN_HEIGHT / 2;
					paddle.x = SCREEN_WIDTH / 2;
				}
			}

			long time2 = System.currentTimeMillis();
			double elapsedTime = time2 - time1;

			lastFt = elapsedTime;

			double seconds = elapsedTime / 1000.0;
			if (seconds > 0.0) {
				double fps = 1.0 / seconds;
				this.setTitle("FPS: " + fps);
			}

		}

		this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

	}

	private void update() {

		currentSlice += lastFt;

		for (; currentSlice >= FT_SLICE; currentSlice -= FT_SLICE) {

			ball.update(scoreboard, paddle);
			paddle.update();
			testCollision(paddle, ball);
			scoreboard.updateScoreboard();
			Iterator<Brick> it = bricks.iterator();
			while (it.hasNext()) {
				Brick brick = it.next();
				testCollision(brick, ball, scoreboard);

				if (brick.destroyed) {
					it.remove();

				}

			}

		}
	}

	private void drawScene(Ball ball, List<Brick> bricks, ScoreBoard scoreboard) {
		// Code for the drawing goes here.
		BufferStrategy bf = this.getBufferStrategy();
		Graphics g = null;

		try {

			g = bf.getDrawGraphics();

			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());

			ball.draw(g);
			paddle.draw(g);
			for (Brick brick : bricks) {
				brick.draw(g);
			}
			scoreboard.draw(g);

		} finally {
			g.dispose();
		}

		bf.show();

		Toolkit.getDefaultToolkit().sync();

	}

	@Override
	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			running = false;
		}
		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
			tryAgain = true;
			// Pongo las fichas a 0
			ball_velocity = 0.3;
			scoreboard.fichas = 0;
		}
		switch (event.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			paddle.moveLeft();
			break;
		case KeyEvent.VK_RIGHT:
			paddle.moveRight();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
			paddle.stopMove();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	public static void main(String[] args) {
		new Arkanoid().run();
	}

}