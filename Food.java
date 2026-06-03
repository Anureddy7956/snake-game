import java.awt.Point;
import java.util.Random;

/**
 * Represents the food that spawns on the game board.
 * Holds position and provides logic for random spawning.
 */
public class Food {
    private Point position;
    private final Random random;

    public Food() {
        this.position = new Point(0, 0);
        this.random = new Random();
    }

    /**
     * Spawns food at a random coordinate on the grid, avoiding the snake's body.
     * @param gridWidth Number of columns on the grid
     * @param gridHeight Number of rows on the grid
     * @param snakeBody List of coordinates occupied by the snake
     */
    public void spawn(int gridWidth, int gridHeight, java.util.List<Point> snakeBody) {
        boolean valid = false;
        while (!valid) {
            int x = random.nextInt(gridWidth);
            int y = random.nextInt(gridHeight);
            position.setLocation(x, y);

            // Ensure the food doesn't spawn inside the snake's body
            valid = true;
            for (Point segment : snakeBody) {
                if (segment.equals(position)) {
                    valid = false;
                    break;
                }
            }
        }
    }

    public Point getPosition() {
        return position;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }
}
