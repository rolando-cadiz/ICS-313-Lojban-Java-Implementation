import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;


public class Lojban {
    private static List<String> types;
    private static List<Boolean> fatciList; 
    private static List<String> value;

    private static void initializeTypes(int size) {
        types = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            types.add(i,"");
        }
    }
    private static void initializeValue(int size) {
        value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) value.add(i, null);
    }
    private static void initializeFactiList(int size) {
        fatciList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            fatciList.add(i,false);
        }
    }

    private static boolean isLojbanVowel(char c) {
        return "aeiouy".indexOf(Character.toLowerCase(c)) != -1;
    }

    private static boolean isLojbanConsonant(char c) {
        return "bcdfgjklmnprstvxz’".indexOf(Character.toLowerCase(c)) != -1;
    }

    private static boolean isNumeric(String s) {
        return s.matches("0|(-?[1-9]\\d*)");
    }


    private static void parseStatements(List<String> tokens) {
        int i = 0;
        while (i < tokens.size()) {
            if (!tokens.get(i).equals("\n") || !tokens.get(i).equals(" ")) {
            parseWord(i,tokens.get(i));
            }
            i++;
        }
        
    }

    private static void parseWord(int index, String token) {
        String charType = "";
        if (token.charAt(0) == '.' && token.charAt(token.length()-1) == '.'){
                types.set(index,"name");
                return;
        }
        if (isNumeric(token)) {
                types.set(index,"number");
                return;
        } 
        for (int i = 0; i < token.length(); i++){
            if (isLojbanVowel(token.charAt(i))) {
                charType += "V";
            } else if (isLojbanConsonant(token.charAt(i))) {
                charType += "C";
            } else {
                System.out.println("Invalid character in short/predicate: " + token);
            }
        }
        if(charType.equals("CV")){
                types.set(index,"short");
            } else if (charType.equals("CVCCV") || charType.equals("CCVCV")){
                types.set(index,"predicate");
            }
    }

    public static boolean implementLo(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("lo")) {
                if (!types.get(i + 1).equals("name")){
                    return false;
                }
            }
        }
        return true;
        
    }

    public static void implementSe(List<String> tokens) {
        String temp = "";
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("se")) {
                if(types.get(i+2).equals("predicate")){
                    temp = tokens.get(i+3);
                    tokens.set(i +3, tokens.get(i+1));
                    tokens.set(i +1, temp);
                } else {
                    temp = tokens.get(i-2);
                    tokens.set(i-2, tokens.get(i+1));
                    tokens.set(i +1, temp);
                }
            }
        }
    }

    public static void implementFacti(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("facti")) {
                fatciList.add(i-1,true);
            }
        }
    }

    public static void implementSumji(List<String> tokens) {
        int a,b,c;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("sumji")) {
                a = Integer.parseInt(tokens.get(i+1));
                b = Integer.parseInt(tokens.get(i+2));
                c = a + b;
                value.set(i-1, Integer.toString(c));
                
            }
        }
    }
    
    public static void implementVujni(List<String> tokens) {
        int a,b,c;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("vujni")) {
                a = Integer.parseInt(tokens.get(i+1));
                b = Integer.parseInt(tokens.get(i+2));
                c = a - b;
                value.set(i-1, Integer.toString(c));
                    
            }
        }

    }

    public static void implementDunli(List<String> tokens){
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals("dunli")) {
                value.set(i-1, tokens.get(i+1));           
            }
        }
    }
    
    





    


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