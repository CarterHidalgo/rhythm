/*
 * Name: App.java
 * Author: Pigpen
 * 
 * Purpose: Start the UCI thread and initialize engine model
 */

package base;

import model.Model;
import uci.UCI;

public class App {
    public static void main(String[] args) {
        UCI.start();
        Model.init();
    }
}

