import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class IO {
    private static final Path wordListPath = Paths.get("./HangmanFunctional/src/main/resources/wordlist.10000.txt");
    private static final Path highScorePath = Paths.get("./HangmanFunctional/src/main/resources/highscores.txt");
    private static final Path artPath = Paths.get("./HangmanFunctional/src/main/resources/hangman_textart.txt");
    @Getter private static final int artLen = 6;
    private static final int SCORES_SHOWN = 5;
    @Getter private static List<String> textArt;
    @Getter private static List<String> wordList;
    @Getter private static List<String> highScores;

    public static void main(String[] args) {
        if(!loadHighScores(SCORES_SHOWN)) {
            System.out.println("Error: High score file missing or corrupted. Expected location: "+highScorePath);
        }

        else if(!loadWords()) {
            System.out.println("Error: Word file missing or corrupted. Expected location: "+wordListPath);
        }

        else if (!loadArt()) {
            System.out.println("Error: Art file missing or corrupted. Expected location: "+artPath);
        }

        else {
            System.out.println("H A N G M A N");
            System.out.println("Credit to MIT (https://www.mit.edu/~ecprice/wordlist.10000) for their Word list");
            System.out.println();

            Game.playGame();
        }
    }

    protected static boolean loadArt() {
        try {
            textArt = Files.readAllLines(artPath);
            return true;
        } catch(IOException e) {return false;}
    }

    protected static boolean saveHighScore(Scanner input, int score) {
        System.out.println("Congratulations on your score ("+score+"), please enter your name: ");
        String name = input.nextLine();

        if(name.contains(":")) {
            System.out.println("Your name contains invalid characters (:), please try again:");
            return saveHighScore(input, score);
        }

        highScores.add(name + ":" + score);
        //Print high score list
        System.out.println(
                highScores.stream()
                .map(s -> " - " + s.replaceAll(":", ": "))
                .sorted(Comparator.comparingInt(s -> Integer.parseInt(((String) s).split(": ")[1])).reversed()
                )
                .reduce("", (a, b) -> a + "\n" + b)
        );

        try {
            Files.write(highScorePath, highScores, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        } catch(IOException e) {
            System.out.println("Sorry, your score vanished into the ether! Bye!");
            return false;
        }
        return true;
    }

    /**
     * Loads the highscores, and prints out the top x
     * @param scoresToShow Number of scores to print out, must be >0
     * @return true if successful
     */
    protected static boolean loadHighScores(int scoresToShow) {
        try {
            highScores = Files.readAllLines(highScorePath);
            System.out.println("High Scores ------------------");
            System.out.println(
                highScores.stream()
                        .map(s -> " - " + s.replaceAll(":", ": "))
                        .sorted(Comparator.comparingInt(s -> Integer.parseInt(((String) s).split(": ")[1])).reversed()
                        )
                        .limit(scoresToShow)
                        .reduce("", (a, b) -> a + "\n" + b).trim()
            );
            System.out.println();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Asks the player if they'd like to continue
     * @param input Scanner to get user input from
     * @return true for yes, false for no
     */
    protected static boolean userWillContinue(Scanner input) {
        String response;
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
     * @return The inputted char
     */
    protected static Character getCharInput(Scanner input, Set<Character> invalidChar) {
        String given = input.nextLine().toLowerCase();
        if(given.length() != 1) {
            System.out.println("That's not a letter! Choose again.");
            return getCharInput(input, invalidChar);
        }

        char toReturn = given.charAt(0);
        if(invalidChar.contains(toReturn)) {
            System.out.println("You have already guessed that letter. Choose again.");
            return getCharInput(input, invalidChar);
        }

        if(toReturn < 'a' || toReturn > 'z') {
            System.out.println("That's not a letter! Choose again.");
            return getCharInput(input, invalidChar);
        }

        return toReturn;
    }

    /**
     * Reads all words in from file, inputs them into wordArray
     * @return true if successful
     */
    protected static boolean loadWords() {
        try {
            wordList = Files.readAllLines(wordListPath);
            return true;
        } catch(IOException e) {return false;}
    }

    /**
     * Selects a random String in wordList[]
     * @return Returns the String selected
     */
    protected static String getRandomWord() {
        return wordList.get((int)(Math.random() * wordList.size()));
    }

    /**
     *
     * @param word The word/solution being guessed
     * @param partsShowing Number of body parts (wrong guesses)
     * @param shownChar Characters already guessed
     * @return The number of 'spaces' remaining to guess, or -1 if the main.java.Hangman is dead
     */
    protected static int displayHangman(String word, int partsShowing, Set<Character> shownChar, String missedLettersStr) {
        //Display Hangman graphic
        System.out.println(textArt.subList(artLen * partsShowing, artLen * (partsShowing+1)).stream().reduce("", (a, b) -> a + "\n" + b));

        System.out.println();
        System.out.println("Missed letters: "+missedLettersStr);
        System.out.println();

        //Display the word hint
        String hint = word.chars().mapToObj(i -> shownChar.contains((char)i) ? (char)i : '_')
                        .reduce(new StringBuilder(),
                                StringBuilder::append,
                                StringBuilder::append).toString();
        System.out.println(hint);
        System.out.println();

        return partsShowing >= 5 ? -1 : (int) hint.chars().filter(ch -> ch == '_').count();
    }
}
