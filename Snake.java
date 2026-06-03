import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the Snake in the game.
 * Manages its position, direction, growth, and body segments.
 */
public class Snake {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private final LinkedList<Point> body;
    private Direction direction;
    private Direction nextDirection; // To prevent rapid double keypresses causing 180-degree turns
    private boolean growOnNextMove;

    /**
     * Initializes the snake with a starting position and default direction.
     * @param startX Initial X coordinate in grid units
     * @param startY Initial Y coordinate in grid units
     */
    public Snake(int startX, int startY) {
        body = new LinkedList<>();
        // Start with a length of 3 segments
        body.add(new Point(startX, startY));
        body.add(new Point(startX - 1, startY));
        body.add(new Point(startX - 2, startY));
        
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        growOnNextMove = false;
    }

    /**
     * Updates the snake's direction if the turn is valid (not 180 degrees).
     * @param newDirection The desired new direction
     */
    public void setDirection(Direction newDirection) {
        // Prevent opposite direction turns
        if (newDirection == Direction.UP && direction != Direction.DOWN) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.DOWN && direction != Direction.UP) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.LEFT && direction != Direction.RIGHT) {
            nextDirection = newDirection;
        } else if (newDirection == Direction.RIGHT && direction != Direction.LEFT) {
            nextDirection = newDirection;
        }
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Moves the snake one step in its current direction.
     * Handles growth if the snake recently ate food.
     */
    public void move() {
        // Apply the buffered direction change
        direction = nextDirection;

        Point head = body.getFirst();
        Point newHead = new Point(head);

        switch (direction) {
            case UP:
                newHead.y--;
                break;
            case DOWN:
                newHead.y++;
                break;
            case LEFT:
                newHead.x--;
                break;
            case RIGHT:
                newHead.x++;
                break;
        }

        // Add new head segment
        body.addFirst(newHead);

        // If not growing, remove the tail segment
        if (growOnNextMove) {
            growOnNextMove = false; // Reset flag
        } else {
            body.removeLast();
        }
    }

    /**
     * Schedules the snake to grow by one segment on its next movement.
     */
    public void grow() {
        growOnNextMove = true;
    }

    /**
     * Returns the head coordinate of the snake.
     */
    public Point getHead() {
        return body.getFirst();
    }

    /**
     * Returns all coordinates of the snake's body.
     */
    public List<Point> getBody() {
        return body;
    }

    /**
     * Checks if the snake has collided with itself.
     * @return true if the head is at the same location as any other body segment.
     */
    public boolean checkSelfCollision() {
        Point head = getHead();
        // Start checking from index 1 (skip head itself)
        for (int i = 1; i < body.size(); i++) {
            if (head.equals(body.get(i))) {
                return true;
            }
        }
        return false;
    }
}
