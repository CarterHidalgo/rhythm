package model;

import java.io.File;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test 
    void testSmallPerftSuite() {
        File smallPerftSuite = new File("app/src/test/resources/perftsuite_small.epd");
        File directory = new File("./");

        // Assumptions.assumeTrue(smallPerftSuite.exists(), "\"" + smallPerftSuite.getPath() + "\" does not exist");
        
    }
}
