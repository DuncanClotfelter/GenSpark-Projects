import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Hangman {
    protected static final String[] wordList = new String[10000];
    private static final String wordListPath = "./Hangman/src/main/resources/wordlist.10000.txt";

    protected String secretWord;
    private final Scanner input;

    public static void main(String[] args) {
        System.out.println("H A N G M A N");
        System.out.println("Credit to MIT (https://www.mit.edu/~ecprice/wordlist.10000) for their Word list");
        System.out.println();

        //Load words from our text file
        if(!loadWords()) {
            System.out.println("Error: Word file missing or corrupted. Expected location: "+wordListPath);
            return;
        }

        new Hangman(System.in, true);
    }

    public Hangman(InputStream userInput, boolean play) {
        if(!play) {input = null; return;}
        input = new Scanner(userInput);
        this.playGame();
    }

    /**
     * Starts a Hangman game, using System.in as user input
     * May recurse
     */
    private void playGame() {
        Set<Character> guesses = new HashSet<>();
        StringBuilder missedChars = new StringBuilder();
        String correctWord = getRandomWord();

        int spacesRemaining;

        //Guess loop
        while((spacesRemaining =
                displayHangman(correctWord, missedChars.length(), guesses, missedChars.toString())) > 0) {
            System.out.println();
            System.out.println("Guess a letter.");
            Character guess;

            while((guess = getCharInput(input, guesses)) == null) {
                //Error message now handled in getCharInput//
            }
            guesses.add(guess);

            if(correctWord.indexOf(guess) == -1) {//Wrong guess
                missedChars.append(guess);
            }
        }

        if(spacesRemaining == 0) {//All letters guessed successfully
            System.out.println("Yes! The secret word is \""+correctWord+"\"! You have won!");
        } else {//loser loser pants on fuser
            System.out.println("No! The secret word is \""+correctWord+"\"! You have lost!");
        }

        if(userWillContinue(input))
            playGame();
    }

    /**
     * Asks the player if they'd like to continue
     * @param input Scanner to get user input from
     * @return true for yes, false for no
     */
    protected boolean userWillContinue(Scanner input) {
        String response = "maybe";
        do {
            System.out.println("Do you want to play again? (yes or no)");
            response = input.nextLine().toLowerCase();
        } while(!response.equals("yes") && !response.equals("no"));
        return response.equals("yes");
    }

    /**
     * Retrieves a single lowercase alphabetical char from the user
     * @param input Scanner object to read from
     * @param invalidChar Set of invalid Characters, nonnull
     * @return The inputted char if valid, otherwise null
     */
    protected static Character getCharInput(Scanner input, Set<Character> invalidChar) {
        String given = input.nextLine().toLowerCase();
        if(given.length() != 1) {
            System.out.println("That's not a letter! Choose again.");
            //Input not a single character
            return null;
        }

        char toReturn = given.charAt(0);
        if(invalidChar.contains(toReturn)) {
            System.out.println("You have already guessed that letter. Choose again.");
            return null;
        }

        if(toReturn < 'a' || toReturn > 'z') {
            System.out.println("That's not a letter! Choose again.");
            //Invalid char
            return null;
        }

        return toReturn;
    }

    /**
     * Reads all words in from file, inputs them into wordArray
     * @return true if successful
     */
    protected static boolean loadWords() {
        try(BufferedReader br = new BufferedReader(new FileReader(wordListPath))) {
            for(int i = 0; i < wordList.length; i++) {
                wordList[i] = br.readLine();
            }
            return true;
        } catch(FileNotFoundException f) {return false;
        } catch(IOException e) {return false;}
    }

    /**
     * Selects a random String in wordList[]
     * @return Returns the String selected
     */
    protected static String getRandomWord() {
        return wordList[(int)(Math.random() * wordList.length)];
    }

    /**
     *
     * @param word The word/solution being guessed
     * @param partsShowing Number of body parts (wrong guesses)
     * @param shownChar Characters already guessed
     * @return The number of 'spaces' remaining to guess, or -1 if the main.java.Hangman is dead
     */
    static int displayHangman(String word, int partsShowing, Set<Character> shownChar, String missedLettersStr) {
        //Display main.java.Hangman graphic
        System.out.println("+---+");
        System.out.println();
        if(partsShowing < 1)
            System.out.println("    |");
        else
            System.out.println("O   |");

        System.out.println();

        if(partsShowing < 2)
            System.out.println("    |");
        else
            System.out.println("|\\\\ |");

        System.out.println();

        if(partsShowing < 3)
            System.out.println("    |");
        else
            System.out.println("|   |");

        if(partsShowing < 4)
            System.out.println("    |");
        else
            System.out.println("|   |");

        if(partsShowing < 5)
            System.out.println("  ===");
        else
            System.out.println("\\\\  ===");

        System.out.println();
        System.out.println("Missed letters: "+missedLettersStr);
        System.out.println();

        //Display the word hint
        int spaces = 0;
        for(char c : word.toCharArray()) {
            if(shownChar.contains(c)) {
                System.out.print(c);
            } else {
                System.out.print('_');
                spaces++;
            }
            System.out.print(' ');
        }

        System.out.println();

        return partsShowing >= 5 ? -1 : spaces;
    }
}
