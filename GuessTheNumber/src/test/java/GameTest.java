import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.Scanner;

public class GameTest extends Game {
    public static void main(String[] args) {
        GameTest test = new GameTest("dev");
        test.testRandomness();
        test.testHints();
        test.testUserConfirmation();
        test.testIntInput();
    }

    @Test
    private void testRandomness() {
        for(int i = 0; i < 100; i++) {
            assertTrue(getRandomInt() >= MIN_GUESS &&
                            getRandomInt() <= MAX_GUESS);
        }
    }

    @Test
    private void testHints() {
        Random r = new Random();
        int randomGuess = r.nextInt();
        assertEquals(getGuessHint(randomGuess-1, randomGuess, 1),
                "Your guess is too low.");

        randomGuess = r.nextInt();
        assertEquals(getGuessHint(randomGuess+1, randomGuess, 1),
                "Your guess is too high.");

        assertTrue(getGuessHint(0, 0, 0).startsWith("Good job"));
    }

    @Test
    private void testUserConfirmation() {
        assertTrue(userWillContinue(setUserInput("y")));
        assertFalse(userWillContinue(setUserInput("n")));
    }

    @Test
    private void testIntInput() {
        assertEquals(-1, getIntInput(setUserInput("" + (MAX_GUESS + 1))));
        assertEquals(-1, getIntInput(setUserInput("" + (MIN_GUESS - 1))));
        assertEquals(-1, getIntInput(setUserInput("yeehaw")));
        assertEquals(MAX_GUESS, getIntInput(setUserInput("" + MAX_GUESS)));
        assertEquals(MIN_GUESS, getIntInput(setUserInput("" + MIN_GUESS)));
    }

    private static Scanner setUserInput(String toSet) {
        return new Scanner(new ByteArrayInputStream(toSet.getBytes()));
    }

    private GameTest(String userName) {
        super(userName, false);
    }
}
