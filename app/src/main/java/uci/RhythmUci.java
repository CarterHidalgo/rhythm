package uci;

public class RhythmUci implements UciListener {
    @Override
    public String getEngineName() {
        return "rhythm";
    }

    @Override
    public String getAuthorName() {
        return "Pigpen";
    }

    @Override
    public void setPosition(String initialPosition, String[] moves) {
        System.out.println("setting up position...");
    }

    @Override
    public String go(GoParameters parameters) {
        return "e2e4"; 
    }
}
