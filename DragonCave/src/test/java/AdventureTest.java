import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdventureTest {
    public static void main(String[] args) {
        testResult();
    }

    @Test
    private static void testResult() {
        int result = Adventure.printStory();
        assertTrue(result >= 0 && result <= 2);
    }
}
