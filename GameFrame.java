import javax.swing.*;

/**
 * Represents the main application window for the Snake game.
 * Sets up the JFrame, configures visual properties, and holds the game canvas.
 */
public class GameFrame extends JFrame {
    
    public GameFrame() {
        // Initialize JFrame properties
        setTitle("Neon Snake - Retro Grid Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Add the main GamePanel where the logic and drawing reside
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        
        // Adjust the frame size automatically to accommodate the component's preferred size
        pack();
        
        // Center the window on the desktop
        setLocationRelativeTo(null);
    }
}
