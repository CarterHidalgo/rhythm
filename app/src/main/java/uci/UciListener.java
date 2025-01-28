/*
 * Name: UciListner.java
 * Author: FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI) Thanks! =]
 * 
 * Purpose: Interface for defining engine-specific UCI listner class (see RhythmUci.java)
 */


package uci;

import java.util.LinkedList;

public interface UciListener {
    default void onConnection() {}

    default Iterable<String> listSupportedOptions() {
        return new LinkedList<>();
    }

    default void setOptionValue(String option) {}

    default void getReady() {}
    
    String getEngineName();

    String getAuthorName();

    void setPosition(String initialPosition, String[] moves);

    String go(GoParameters parameters);

    void print(String param);

    int perft(int depth);
}