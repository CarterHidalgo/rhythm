package base;

import uci.RhythmUci;
import uci.UciProtocol;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting up...");

        RhythmUci rhythmUci = new RhythmUci();
        UciProtocol uci = new UciProtocol(rhythmUci);
    }
}

