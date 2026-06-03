import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The view and controller component of the Snake game.
 * Renders the game board, overlays, score, and handles keyboard inputs and game loop timer.
 */
public class GamePanel extends JPanel implements ActionListener {
    private static final int CELL_SIZE = 24;
    private static final int GRID_WIDTH = 25;
    private static final int GRID_HEIGHT = 25;
    private static final int HEADER_HEIGHT = 60;
    
    private final GameLogic gameLogic;
    private final Timer gameTimer;
    private final List<Particle> particles;

    // Pulse animation helper
    private double foodPulse = 0;
    private int pulseDirection = 1;

    public GamePanel() {
        this.gameLogic = new GameLogic(GRID_WIDTH, GRID_HEIGHT);
        this.particles = new ArrayList<>();

        // Set up panel properties
        setPreferredSize(new Dimension(GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE + HEADER_HEIGHT));
        setBackground(new Color(15, 23, 42)); // Tailwind Slate-900
        setFocusable(true);
        
        // Register key listeners
        addKeyListener(new GameKeyAdapter());

        // Initialize Timer. The timer delay is set dynamically based on difficulty.
        // It starts with MEDIUM difficulty delay.
        this.gameTimer = new Timer(gameLogic.getDifficulty().getDelay(), this);
        // Start the timer immediately; the timer will tick, but update() won't do anything unless state is RUNNING
        gameTimer.start();
    }

    /**
     * Resets and starts the timer based on the selected difficulty.
     */
    private void updateTimerDelay() {
        gameTimer.setDelay(gameLogic.getDifficulty().getDelay());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameLogic.getState() == GameLogic.GameState.RUNNING) {
            int oldScore = gameLogic.getScore();
            Point oldFoodPos = new Point(gameLogic.getFood().getPosition());

            // Run game logic update
            gameLogic.update();

            // Check if food was eaten (score increased)
            if (gameLogic.getScore() > oldScore) {
                // Spawn particle burst at the food's old position
                spawnFoodExplosion(oldFoodPos.x * CELL_SIZE + CELL_SIZE / 2,
                                   oldFoodPos.y * CELL_SIZE + HEADER_HEIGHT + CELL_SIZE / 2);
            }
        }

        // Update food pulse animation state
        foodPulse += pulseDirection * 0.15;
        if (foodPulse > 4.0 || foodPulse < 0) {
            pulseDirection *= -1;
        }

        // Update particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead()) {
                particles.remove(i);
            }
        }

        // Request repaint
        repaint();
    }

    /**
     * Spawns a burst of particles when food is eaten.
     */
    private void spawnFoodExplosion(int x, int y) {
        Color particleColor = new Color(244, 63, 94); // Neon Rose
        for (int i = 0; i < 15; i++) {
            particles.add(new Particle(x, y, particleColor));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw Board Area
        drawBoard(g2d);

        // 2. Draw Header/Scoreboard Area
        drawHeader(g2d);

        // 3. Draw Game Objects if running or paused or game over
        if (gameLogic.getState() != GameLogic.GameState.START) {
            drawFood(g2d);
            drawSnake(g2d);
            
            // Draw particles
            for (Particle p : particles) {
                p.draw(g2d);
            }
        }

        // 4. Draw Screen Overlays based on state
        switch (gameLogic.getState()) {
            case START:
                drawStartScreen(g2d);
                break;
            case PAUSED:
                drawPauseOverlay(g2d);
                break;
            case GAME_OVER:
                drawGameOverScreen(g2d);
                break;
            case RUNNING:
                // No overlay, game is active
                break;
        }
    }

    /**
     * Draws the background grid for the snake board.
     */
    private void drawBoard(Graphics2D g2d) {
        // Fill board background
        g2d.setColor(new Color(15, 23, 42)); // Slate-900
        g2d.fillRect(0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);

        // Draw grid lines
        g2d.setColor(new Color(30, 41, 59, 120)); // Slate-800 with transparency
        for (int i = 0; i <= GRID_WIDTH; i++) {
            // Vertical lines
            g2d.drawLine(i * CELL_SIZE, HEADER_HEIGHT, i * CELL_SIZE, getHeight());
        }
        for (int j = 0; j <= GRID_HEIGHT; j++) {
            // Horizontal lines
            g2d.drawLine(0, HEADER_HEIGHT + j * CELL_SIZE, getWidth(), HEADER_HEIGHT + j * CELL_SIZE);
        }
    }

    /**
     * Draws the top header scoreboard.
     */
    private void drawHeader(Graphics2D g2d) {
        // Draw header background
        g2d.setColor(new Color(30, 41, 59)); // Slate-800
        g2d.fillRect(0, 0, getWidth(), HEADER_HEIGHT);

        // Draw a separator line
        g2d.setColor(new Color(51, 65, 85)); // Slate-700
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, HEADER_HEIGHT - 1, getWidth(), HEADER_HEIGHT - 1);
        g2d.setStroke(new BasicStroke(1)); // Reset stroke

        // Draw Score texts
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Current score (Left side)
        g2d.setColor(new Color(148, 163, 184)); // Slate-400
        g2d.drawString("SCORE: ", 24, 36);
        g2d.setColor(new Color(34, 197, 94)); // Emerald-500
        g2d.drawString(String.valueOf(gameLogic.getScore()), 95, 36);

        // High score (Right side)
        g2d.setColor(new Color(148, 163, 184));
        String highScoreText = "HIGH SCORE: ";
        int textWidth = g2d.getFontMetrics().stringWidth(highScoreText);
        g2d.drawString(highScoreText, getWidth() - 150 - textWidth, 36);
        g2d.setColor(new Color(234, 179, 8)); // Yellow-500
        g2d.drawString(String.valueOf(gameLogic.getHighScore()), getWidth() - 140, 36);

        // Draw difficulty tag
        String diffText = "DIFFICULTY: " + gameLogic.getDifficulty().name();
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2d.setColor(new Color(148, 163, 184));
        int diffWidth = g2d.getFontMetrics().stringWidth(diffText);
        g2d.drawString(diffText, (getWidth() - diffWidth) / 2, 34);
    }

    /**
     * Draws the food with a glowing, pulsing look.
     */
    private void drawFood(Graphics2D g2d) {
        Food food = gameLogic.getFood();
        int px = food.getX() * CELL_SIZE;
        int py = food.getY() * CELL_SIZE + HEADER_HEIGHT;

        // Glowing outer ring
        int glowSize = (int) (CELL_SIZE + foodPulse * 1.5);
        int glowOffset = (glowSize - CELL_SIZE) / 2;
        g2d.setColor(new Color(244, 63, 94, 60)); // Neon rose with low alpha
        g2d.fillOval(px - glowOffset, py - glowOffset, glowSize, glowSize);

        // Inner solid food
        g2d.setColor(new Color(244, 63, 94)); // Solid Neon Rose
        g2d.fillOval(px + 4, py + 4, CELL_SIZE - 8, CELL_SIZE - 8);

        // Highlighting dot to make it look 3D
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.fillOval(px + 8, py + 8, 4, 4);
    }

    /**
     * Draws the snake with gradient colors and head details (eyes).
     */
    private void drawSnake(Graphics2D g2d) {
        Snake snake = gameLogic.getSnake();
        List<Point> body = snake.getBody();
        int size = body.size();

        for (int i = 0; i < size; i++) {
            Point segment = body.get(i);
            int px = segment.x * CELL_SIZE;
            int py = segment.y * CELL_SIZE + HEADER_HEIGHT;

            // Generate gradient color from Head (Neon Green/Emerald) to Tail (Neon Cyan)
            float ratio = (float) i / Math.max(1, size - 1);
            Color segmentColor = interpolateColor(
                    new Color(34, 197, 94),  // Emerald-500
                    new Color(6, 182, 212),  // Cyan-500
                    ratio
            );

            g2d.setColor(segmentColor);
            
            // Draw rounded cell
            g2d.fillRoundRect(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4, 8, 8);

            // Draw head details (eyes)
            if (i == 0) {
                drawSnakeEyes(g2d, px, py, snake.getDirection());
            }
        }
    }

    /**
     * Draws small eyes on the snake head based on direction.
     */
    private void drawSnakeEyes(Graphics2D g2d, int px, int py, Snake.Direction dir) {
        g2d.setColor(Color.WHITE);
        int eyeSize = 4;
        
        switch (dir) {
            case UP:
                g2d.fillOval(px + 6, py + 6, eyeSize, eyeSize);
                g2d.fillOval(px + CELL_SIZE - 10, py + 6, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(px + 7, py + 6, 2, 2);
                g2d.fillOval(px + CELL_SIZE - 9, py + 6, 2, 2);
                break;
            case DOWN:
                g2d.fillOval(px + 6, py + CELL_SIZE - 10, eyeSize, eyeSize);
                g2d.fillOval(px + CELL_SIZE - 10, py + CELL_SIZE - 10, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(px + 7, py + CELL_SIZE - 9, 2, 2);
                g2d.fillOval(px + CELL_SIZE - 9, py + CELL_SIZE - 9, 2, 2);
                break;
            case LEFT:
                g2d.fillOval(px + 6, py + 6, eyeSize, eyeSize);
                g2d.fillOval(px + 6, py + CELL_SIZE - 10, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(px + 6, py + 7, 2, 2);
                g2d.fillOval(px + 6, py + CELL_SIZE - 9, 2, 2);
                break;
            case RIGHT:
                g2d.fillOval(px + CELL_SIZE - 10, py + 6, eyeSize, eyeSize);
                g2d.fillOval(px + CELL_SIZE - 10, py + CELL_SIZE - 10, eyeSize, eyeSize);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(px + CELL_SIZE - 9, py + 7, 2, 2);
                g2d.fillOval(px + CELL_SIZE - 9, py + CELL_SIZE - 9, 2, 2);
                break;
        }
    }

    /**
     * Linearly interpolates between two colors.
     */
    private Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(r, g, b);
    }

    /**
     * Renders the Start screen.
     */
    private void drawStartScreen(Graphics2D g2d) {
        // Translucent overlay
        g2d.setColor(new Color(15, 23, 42, 240));
        g2d.fillRect(0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);

        // Title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 42));
        g2d.setColor(Color.WHITE);
        String title = "NEON SNAKE";
        int titleWidth = g2d.getFontMetrics().stringWidth(title);
        
        // Neon title shadow effect
        g2d.setColor(new Color(34, 197, 94, 100)); // Glowing green shadow
        g2d.drawString(title, (getWidth() - titleWidth) / 2 + 2, 200 + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 200);

        // Subtitle
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2d.setColor(new Color(148, 163, 184)); // Slate-400
        String startText = "PRESS [SPACE] TO PLAY";
        int startWidth = g2d.getFontMetrics().stringWidth(startText);
        g2d.drawString(startText, (getWidth() - startWidth) / 2, 280);

        // Instructions
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(new Color(94, 234, 212)); // Teal-300
        String instructions = "Controls: Use Arrow Keys or WASD to turn";
        int instWidth = g2d.getFontMetrics().stringWidth(instructions);
        g2d.drawString(instructions, (getWidth() - instWidth) / 2, 350);

        String pauseInstruction = "Press [SPACE] to pause/resume in-game";
        int pauseWidth = g2d.getFontMetrics().stringWidth(pauseInstruction);
        g2d.drawString(pauseInstruction, (getWidth() - pauseWidth) / 2, 380);

        // Difficulty selector instructions
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        String diffTitle = "SELECT DIFFICULTY:";
        int diffTitleWidth = g2d.getFontMetrics().stringWidth(diffTitle);
        g2d.drawString(diffTitle, (getWidth() - diffTitleWidth) / 2, 450);

        // Show keys
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String keysText = "Press: [1] EASY  |  [2] MEDIUM  |  [3] HARD";
        int keysWidth = g2d.getFontMetrics().stringWidth(keysText);
        
        // Highlight active difficulty
        g2d.setColor(new Color(148, 163, 184));
        g2d.drawString(keysText, (getWidth() - keysWidth) / 2, 480);

        // Draw selected indicator
        String activeText = "Active Level: " + gameLogic.getDifficulty().name();
        g2d.setColor(new Color(234, 179, 8)); // Yellow-500
        int activeWidth = g2d.getFontMetrics().stringWidth(activeText);
        g2d.drawString(activeText, (getWidth() - activeWidth) / 2, 510);
    }

    /**
     * Renders the Pause overlay.
     */
    private void drawPauseOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(15, 23, 42, 180));
        g2d.fillRect(0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2d.setColor(new Color(56, 189, 248)); // Sky-400
        String text = "GAME PAUSED";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (getWidth() - textWidth) / 2, 300);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        String resumeText = "Press [SPACE] to Resume";
        int resumeWidth = g2d.getFontMetrics().stringWidth(resumeText);
        g2d.drawString(resumeText, (getWidth() - resumeWidth) / 2, 350);
    }

    /**
     * Renders the Game Over screen.
     */
    private void drawGameOverScreen(Graphics2D g2d) {
        g2d.setColor(new Color(24, 15, 25, 245)); // Slate background tinted dark crimson
        g2d.fillRect(0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 42));
        g2d.setColor(new Color(239, 68, 68)); // Red-500
        String text = "GAME OVER";
        int textWidth = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, (getWidth() - textWidth) / 2, 220);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2d.setColor(Color.WHITE);
        String scoreStr = "Final Score: " + gameLogic.getScore();
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreStr);
        g2d.drawString(scoreStr, (getWidth() - scoreWidth) / 2, 280);

        if (gameLogic.getScore() >= gameLogic.getHighScore() && gameLogic.getScore() > 0) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.setColor(new Color(234, 179, 8)); // Gold
            String newRecord = "NEW HIGH SCORE!";
            int recordWidth = g2d.getFontMetrics().stringWidth(newRecord);
            g2d.drawString(newRecord, (getWidth() - recordWidth) / 2, 315);
        }

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g2d.setColor(new Color(148, 163, 184)); // Slate-400
        String restartStr = "Press [ENTER] to Restart";
        int restartWidth = g2d.getFontMetrics().stringWidth(restartStr);
        g2d.drawString(restartStr, (getWidth() - restartWidth) / 2, 380);

        String changeDiffStr = "Press [1], [2], or [3] to select a new difficulty";
        int changeDiffWidth = g2d.getFontMetrics().stringWidth(changeDiffStr);
        g2d.drawString(changeDiffStr, (getWidth() - changeDiffWidth) / 2, 420);
        
        String activeText = "Active Level: " + gameLogic.getDifficulty().name();
        g2d.setColor(new Color(234, 179, 8));
        int activeWidth = g2d.getFontMetrics().stringWidth(activeText);
        g2d.drawString(activeText, (getWidth() - activeWidth) / 2, 450);
    }

    /**
     * Keyboard event handler for controlling the game.
     */
    private class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            GameLogic.GameState state = gameLogic.getState();

            // Menu Control
            if (state == GameLogic.GameState.START) {
                if (key == KeyEvent.VK_SPACE) {
                    gameLogic.startNewGame();
                } else if (key == KeyEvent.VK_1) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.EASY);
                    updateTimerDelay();
                } else if (key == KeyEvent.VK_2) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.MEDIUM);
                    updateTimerDelay();
                } else if (key == KeyEvent.VK_3) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.HARD);
                    updateTimerDelay();
                }
                repaint();
                return;
            }

            if (state == GameLogic.GameState.GAME_OVER) {
                if (key == KeyEvent.VK_ENTER) {
                    gameLogic.startNewGame();
                } else if (key == KeyEvent.VK_1) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.EASY);
                    updateTimerDelay();
                } else if (key == KeyEvent.VK_2) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.MEDIUM);
                    updateTimerDelay();
                } else if (key == KeyEvent.VK_3) {
                    gameLogic.setDifficulty(GameLogic.Difficulty.HARD);
                    updateTimerDelay();
                }
                repaint();
                return;
            }

            // In-game controls
            if (state == GameLogic.GameState.RUNNING || state == GameLogic.GameState.PAUSED) {
                if (key == KeyEvent.VK_SPACE) {
                    gameLogic.togglePause();
                    repaint();
                    return;
                }
            }

            if (state == GameLogic.GameState.RUNNING) {
                Snake snake = gameLogic.getSnake();
                if (snake != null) {
                    switch (key) {
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            snake.setDirection(Snake.Direction.UP);
                            break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            snake.setDirection(Snake.Direction.DOWN);
                            break;
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            snake.setDirection(Snake.Direction.LEFT);
                            break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            snake.setDirection(Snake.Direction.RIGHT);
                            break;
                    }
                }
            }
        }
    }

    /**
     * Nested Particle class to manage visually stunning feedback explosion effects.
     */
    private static class Particle {
        double x, y;
        double vx, vy;
        int life;
        int maxLife;
        Color color;

        public Particle(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            // Generate speed and direction vectors randomly
            double angle = Math.random() * 2 * Math.PI;
            double speed = 1.0 + Math.random() * 4.0;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
            this.maxLife = 12 + (int) (Math.random() * 14);
            this.life = maxLife;
            this.color = color;
        }

        public void update() {
            x += vx;
            y += vy;
            // Slow down particles slightly (friction/air resistance)
            vx *= 0.95;
            vy *= 0.95;
            life--;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void draw(Graphics2D g2d) {
            float alpha = (float) life / maxLife;
            // Draw particle with fading alpha value
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
            int size = (int) (3 + alpha * 5);
            g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
    }
}
