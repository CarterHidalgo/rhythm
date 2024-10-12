package uci;

import java.util.LinkedList;

public interface UciListener {
    default void onConnection() {

    }

    String getEngineName();

    String getAuthorName();

    default Iterable<String> listSupportedOptions() {
        return new LinkedList<>();
    }

    default void setOptionValue(String option) {

    }

    default void getReady() {

    }

    void setPosition(String initialPosition, String[] moves);

    String go(GoParameters parameters);
}