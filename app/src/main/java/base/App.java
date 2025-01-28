/*
 * Name: App.java
 * Author: Pigpen
 * 
 * Purpose: Start the UCI thread and initialize engine model
 */

package base;

import java.io.IOException;

import model.Model;
import uci.RhythmUci;
import uci.UciProtocol;

public class App {
    public static void main(String[] args) throws IOException {
        RhythmUci uci = new RhythmUci();
        UciProtocol uciProtocol = new UciProtocol(uci);

        uciProtocol.start();

        Model.init();
    }

    public void getGreeting() {
        // return "this is a greeting";
        System.out.println("this is a greeting");
    }
}

