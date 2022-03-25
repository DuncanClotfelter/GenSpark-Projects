import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HangmanTest extends Hangman {
    public static void main(String[] args) {
        HangmanTest test = new HangmanTest();
        test.testWordFile();
        test.testCharInput();
        test.testRandomWord();
    }

    @Test
    private void testWordFile() {
        //Make sure the words load correctly
        assertTrue(Hangman.loadWords());

        //Check list is intact
        for(String s : Hangman.wordList) {
            assertNotNull(s);
            assertNotEquals("", s);
        }
    }

    @Test
    private void testRandomWord() {
        //Technically, this is failable with functional code.
        //Just extremely unlikely
        String word = Hangman.getRandomWord();
        for(int i = 0; i < 5; i++)
            assertNotEquals(word, Hangman.getRandomWord());
    }

    @Test
    private void testCharInput() {
        Set<Character> invalidChar = Set.of('b');
        //Too many characters
        assertNull(Hangman.getCharInput(setUserInput("ooga booga"), invalidChar));

        //Already guessed character
        assertNull(Hangman.getCharInput(setUserInput("b"), invalidChar));

        //Not a letter
        assertNull(Hangman.getCharInput(setUserInput("@"), invalidChar));
    }

    private static Scanner setUserInput(String toSet) {
        System.setIn(new ByteArrayInputStream(toSet.getBytes()));
        return new Scanner(System.in);
    }

    private HangmanTest() {
        super(null, false);
    }
}
