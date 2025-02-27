/*
 * Name: App.java Author: Pigpen
 * 
 * Purpose: Start the UCI thread and initialize engine model
 */

package engine.base;

import engine.helper.Printer;
import engine.model.Model;
import engine.uci.UCI;

public class App {
    public static void main(String[] args) {
        Printer.white("");
        UCI.start();
        Model.init();
    }
}
