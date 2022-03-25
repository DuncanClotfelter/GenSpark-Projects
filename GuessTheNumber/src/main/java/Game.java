import java.util.Scanner;

public class Game {
    protected static final Scanner s = new Scanner(System.in);
    protected static final int MIN_GUESS = 1;
    protected static final int MAX_GUESS = 20;
    private final String playerName;

    public static void main(String[] args) {
        System.out.println("Hello! What is your name?");
        String userName = s.nextLine();
        System.out.println("Well, "+userName+" I am thinking of a number between 1 and 20.");

        new Game(userName, true).playGame();
    }

    public Game(String n, boolean play) {
        playerName = n;
        if(play)
            this.playGame();
    }

    private void playGame() {
        int curGuess = -1;
        int guessCount = 0;
        int correctGuess = getRandomInt();
        System.out.println("Take a guess.");

        //Guessing loop
        while (curGuess != correctGuess) {
            if ((curGuess = getIntInput(s)) != -1) {
                guessCount++;
                System.out.println(getGuessHint(curGuess, correctGuess, guessCount));
            } else {//Error message
                System.out.println("WOAAAaHH NELLY!! Slow down there!");
            }
        }

        if(userWillContinue(s)) {
            playGame();
        }
    }


    /**
     * @return Random integer between MIN_GUESS and MAX_GUESS
     */
    protected int getRandomInt() {
        int len = MAX_GUESS - MIN_GUESS;
        return (int)(Math.random() * len) + MIN_GUESS;
    }

    /**
     * Returns a hint to show to the user
     * @param curGuess The user's guess
     * @param correctGuess The destination number
     * @return A String message describing the direction of the number
     */
    protected String getGuessHint(int curGuess, int correctGuess, int guessCount) {
        if (curGuess < correctGuess) {
           return "Your guess is too low.";
        } else if (curGuess > correctGuess) {
            return "Your guess is too high.";
        } else {
            return "Good job, "+playerName+"! You guessed my number in "+guessCount+" guesses!";
        }
    }

    /**
     * Asks the player if they'd like to continue
     * @param input Scanner to get user input from
     * @return true for yes, false for no
     */
    protected static boolean userWillContinue(Scanner input) {
        String response = "maybe";
        do {
            System.out.println("Would you like to play again? (y or n)");
            response = input.nextLine().toLowerCase();
        } while(!response.equals("y") && !response.equals("n"));
        return response.equals("y");
    }

    /**
     * Gets user input as an int via the Scanner
     * @param s Scanner object
     * @return input if successful, -1 otherwise
     */
    protected static int getIntInput(Scanner s) {
        try {
            int input = Integer.parseInt(s.nextLine());
            if(input < MIN_GUESS || input > MAX_GUESS) {
                return -1;
            }
            return input;
        } catch(NumberFormatException e) {
            return -1;
        }
    }
}
