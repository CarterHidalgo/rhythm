package uci;

public class RhythmUci implements UciListener {
    @Override
    public String getEngineName() {
        return "MyChessEngine";
    }

    @Override
    public String getAuthorName() {
        return "Your Name";
    }

    @Override
    public void setPosition(String initialPosition, String[] moves) {
        // Parse the FEN string and set the board position in your engine.
        // Process any moves that have been made.
    }

    @Override
    public String go(GoParameters parameters) {
        // This is where your engine calculates its move.
        // Use the parameters to adjust time controls, depth, etc.
        return "e2e4"; // Example move, replace with actual engine's move.
    }
}
