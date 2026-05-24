/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Start the UCI thread and initialize engine model
 */

package engine.base;

import engine.model.Model;
import engine.model.Printer;
import engine.uci.UCI;

public class App {
    public static final void main(String[] args) {
        Printer.white("");
        UCI.start();
        Model.init();
    }
}
