import java.util.Scanner;
import java.util.Arrays;
import java.util.List;


public class Lojban {

    public static void main(String[] args) {
        System.out.println("Enter your Lojban statement: ");
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.useDelimiter("\\A").next();

        if (!userInput.matches("^[a-z0-9\\.\\s]+$")){
            System.out.println("Invalid character found");
            System.out.println("Only lowercase ASCII, digits 0-9, periods, and whitespaces allowed.");
        }
        if (!userInput.trim().matches("(?s)(i\\s+.+)+")){
            System.out.println("Invalid Lojban structure");
            System.out.println("Lojban statements must start with 'i' followed by at least one word.");
        }

        List<String> tokens = Tokenize.tokenize(userInput);



        
        //System.out.println("Tokens: " + Arrays.toString(tokens.toArray()));
        scanner.close();
    }
}