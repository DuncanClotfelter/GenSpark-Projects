import java.util.Scanner;

public class Adventure {
    public static void main(String[] args) {
        printStory();
    }

    protected static int printStory() {
        //Narrate
        System.out.println("You are in a land full of dragons. In front of you,");
        System.out.println("you see two caves. In one cave, the dragon is friendly");
        System.out.println("and will share his treasure with you. The other dragon");
        System.out.println("is greedy and hungry and will eat you on sight.");
        System.out.println("Which cave will you go into? (1 or 2)");

        //Get user input
        Scanner input = new Scanner(System.in);
        int response = 0;
        try { response = Integer.parseInt(input.nextLine()); }
        catch(NumberFormatException e) {/* leave response as 0 */}

        //Narrate
        System.out.println("You approach the cave...");
        System.out.println("It is dark and spooky...");
        System.out.println("A large dragon jumps out in front of you! He opens his jaws and...");

        //Determine dynamic story ending based on user input
        String result;
        if(response == 1) {
            result = "Gobbles you down in one bite!";
        } else if(response == 2) {
            result = "Bestows upon you a fortune fit for a king!";
        } else {//Any unexpected response
            result = "Hurts itself in its confusion!";
        }

        //Fin
        System.out.println(result);

        return response;
    }
}
