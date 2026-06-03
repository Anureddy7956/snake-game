import javax.swing.*;

/**
 * Entry point for the Snake Game application.
 * Ensures the GUI components are initialized on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // Run UI creation on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
