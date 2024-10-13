package base;

import uci.RhythmUci;
import uci.UciProtocol;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting up...");

        RhythmUci uci = new RhythmUci();
        UciProtocol uciProtocol = new UciProtocol(uci);
    }
}

