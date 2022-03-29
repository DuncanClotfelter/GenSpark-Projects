import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Game {
    private static int score = 0;
    private static final Scanner input = new Scanner(System.in);

    /**
     * Starts a Hangman game, using System.in as user input
     * May recurse
     */
    protected static void playGame() {
        Set<Character> guesses = new HashSet<>();
        StringBuilder missedChars = new StringBuilder();
        String correctWord = IO.getRandomWord();

        int spacesRemaining;

        //Guess loop, only for/while allowed
        while((spacesRemaining =
                IO.displayHangman(correctWord, missedChars.length(), guesses, missedChars.toString())) > 0) {
            System.out.println();
            System.out.println("Guess a letter.");
            Character guess;

            guess = IO.getCharInput(input, guesses);
            guesses.add(guess);

            if(correctWord.indexOf(guess) == -1) {//Wrong guess
                missedChars.append(guess);
            }
        }

        if(spacesRemaining == 0) {//All letters guessed successfully
            System.out.println("Yes! The secret word is \""+correctWord+"\"! You have won!");
            score++;
        } else {//loser loser pants on fuser
            System.out.println("No! The secret word is \""+correctWord+"\"! You have lost!");
        }

        if(IO.userWillContinue(input)) {
            playGame();
        } else if(score == 0) {
            System.out.println("You did not get any words right, and therefore are ineligible for the highscore list. Better luck next time!");
        } else {
            IO.saveHighScore(input, score);
        }
    }
}
