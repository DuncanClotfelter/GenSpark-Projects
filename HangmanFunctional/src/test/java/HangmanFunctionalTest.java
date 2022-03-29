import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HangmanFunctionalTest {
    public static void main(String[] args) {
        testLoadFiles();
        testCharInput();
        testRandomWord();
        testHighScores();
    }

    @Test
    private static void testHighScores() {
        int highScoreLen = IO.getHighScores().size();
        IO.saveHighScore(setUserInput("Duncan"), 99);
        assertEquals(highScoreLen + 1, IO.getHighScores().size());
        IO.loadHighScores(0);
        assertEquals(highScoreLen + 1, IO.getHighScores().size());
    }

    @Test
    private static void testLoadFiles() {
        //Make sure all files load correctly
        assertTrue(IO.loadWords());
        assertTrue(IO.loadArt());
        assertTrue(IO.loadHighScores(0));

        //Check lists are intact
        for(String s : IO.getWordList()) {
            assertNotNull(s);
            assertNotEquals("", s);
        }

        for(String s : IO.getHighScores()) {
            assertNotNull(s);
            assertNotEquals("", s);
            //Using 1 colon to separate name and score
            int colonLoc;
            assertTrue((colonLoc = s.indexOf(":") + 1) > 0);
            assertEquals(-1, s.indexOf(":", colonLoc));
        }

        for(String s : IO.getTextArt()) {
            assertNotNull(s);
            assertNotEquals("", s);
        }
        //Art file should contain "stages" of equal size
        assertNotEquals(IO.getTextArt().size(), 0);
        assertEquals(IO.getTextArt().size() % IO.getArtLen(), 0);
    }

    @Test
    private static void testRandomWord() {
        //Technically, this is failable with functional code.
        //Just extremely unlikely
        String word = IO.getRandomWord();
        for(int i = 0; i < 5; i++)
            assertNotEquals(word, IO.getRandomWord());
    }

    @Test
    private static void testCharInput() {
        Set<Character> invalidChar = Set.of('b');
        //Too many characters
        assertThrows(NoSuchElementException.class, () ->
                IO.getCharInput(setUserInput("ooga booga"), invalidChar)
        );

        //Already guessed character
        assertThrows(NoSuchElementException.class, () ->
                IO.getCharInput(setUserInput("b"), invalidChar)
        );

        //Not a letter
        assertThrows(NoSuchElementException.class, () ->
                IO.getCharInput(setUserInput("@"), invalidChar)
        );

        //Correct scenario
        assertEquals(IO.getCharInput(setUserInput("a"), invalidChar), 'a');
    }

    private static Scanner setUserInput(String toSet) {
        System.setIn(new ByteArrayInputStream(toSet.getBytes()));
        return new Scanner(System.in);
    }
}
