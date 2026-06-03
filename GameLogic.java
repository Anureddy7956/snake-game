import java.awt.Point;

/**
 * Manages the core game state, rules, scoring, and level progression.
 * Separates game logic from presentation.
 */
public class GameLogic {
    public enum GameState {
        START, RUNNING, PAUSED, GAME_OVER
    }

    public enum Difficulty {
        EASY(150),   // 150ms tick rate
        MEDIUM(100), // 100ms tick rate
        HARD(60);    // 60ms tick rate

        private final int delay;

        Difficulty(int delay) {
            this.delay = delay;
        }

        public int getDelay() {
            return delay;
        }
    }

    private final int gridWidth;
    private final int gridHeight;

    private Snake snake;
    private Food food;
    private GameState state;
    private Difficulty difficulty;

    private int score;
    private int highScore;

    public GameLogic(int gridWidth, int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.state = GameState.START;
        this.difficulty = Difficulty.MEDIUM;
        this.score = 0;
        this.highScore = 0;
    }

    /**
     * Initializes a new game session.
     */
    public void startNewGame() {
        // Spawn snake in the center-left of the board
        snake = new Snake(gridWidth / 3, gridHeight / 2);
        
        // Initialize food
        food = new Food();
        food.spawn(gridWidth, gridHeight, snake.getBody());
        
        score = 0;
        state = GameState.RUNNING;
        
        // Placeholder for sound effect: Start game sound
        playSoundPlaceholder("game_start.wav");
    }

    /**
     * Advances the game state by one frame.
     * Updates snake position, checks collisions, and handles score/game over.
     */
    public void update() {
        if (state != GameState.RUNNING) {
            return;
        }

        // Move snake
        snake.move();

        // Check if snake head is out of bounds
        Point head = snake.getHead();
        if (head.x < 0 || head.x >= gridWidth || head.y < 0 || head.y >= gridHeight) {
            triggerGameOver();
            return;
        }

        // Check if snake eats itself
        if (snake.checkSelfCollision()) {
            triggerGameOver();
            return;
        }

        // Check if snake eats food
        if (head.equals(food.getPosition())) {
            snake.grow();
            score += 10;
            if (score > highScore) {
                highScore = score;
            }
            food.spawn(gridWidth, gridHeight, snake.getBody());
            
            // Placeholder for eating sound
            playSoundPlaceholder("eat_food.wav");
        }
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        // Placeholder for game over sound
        playSoundPlaceholder("game_over.wav");
    }

    public void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            playSoundPlaceholder("pause.wav");
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            playSoundPlaceholder("resume.wav");
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * Placeholder method for sound effects and music logic.
     * In a full game, this would interface with a Java Sound clip player.
     * @param clipName Name of the audio file
     */
    public void playSoundPlaceholder(String clipName) {
        // This is a placeholder as requested.
        // It provides a structural hook where developers can integrate custom Clip playback.
    }
}
