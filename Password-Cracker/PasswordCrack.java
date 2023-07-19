import java.nio.file.*;
import java.util.*;
import java.io.*;

public class PasswordCrack {
    static Set<String> foundPasswords = new HashSet<>();
    static Set<String> dictWords = new HashSet<>();
    static Set<String> passwdLines = new HashSet<>();
    static HashSet<Integer> usersDone = new HashSet<>();
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Error: Incorrect number of arguments. Usage: java PasswordCrack <dictionary file> <password file>");
            System.exit(1);
        }
        
        Path dictPath = Paths.get(args[0]);
        Path passwdPath = Paths.get(args[1]);
        
        if (!Files.exists(dictPath) || !Files.exists(passwdPath)) {
            System.err.println("Error: One or both of the specified files do not exist.");
            System.exit(1);
        }
        
        if (!Files.isRegularFile(dictPath) || !Files.isRegularFile(passwdPath)) {
            System.err.println("Error: Both arguments must be regular files.");
            System.exit(1);
        }
        
        try {
            dictWords = new HashSet<>(Files.readAllLines(dictPath));
            passwdLines = new HashSet<>(Files.readAllLines(passwdPath));
        } catch (IOException e) {
            System.err.println("Error: An error occurred while reading the files.");
            System.exit(1);
        }        

        // First name and last name check
        for (String passwdLine : passwdLines) {
            String salt = passwdLine.split(":")[1].substring(0, 2);
            String fullName = passwdLine.split(":")[4];
            String[] names = fullName.split(" ");

            // Add them to the list for later usage
            dictWords.add(names[0]);
            dictWords.add(names[1]);

            if (names[0].length() > 8)
                names[0] = names[0].substring(0, 8);

            if (names[1].length() > 8)
                names[1] = names[1].substring(0, 8);
            

            if (checkWord(salt, names[0], passwdLine)) {
                System.out.println(names[0]);
                foundPasswords.add(names[0]);
                break;
            }
            if (checkWord(salt, names[1], passwdLine)) {
                System.out.println(names[1]);
                foundPasswords.add(names[1]);
                break;
            }
        }

        for (String passwdLine : passwdLines) {
            String salt = passwdLine.split(":")[1].substring(0, 2);

            String[] easyToGuessWords = {"123456", "12121212", "qwerty", "password", "12345", "12345678", "111111", "1234567", "123123", "1q2w3e", "ABCDEF", "0", "abc123", "654321", "123321", "qwerty12", "iloveyou", "666666", "sunshine", "princess", "admin", "welcome", "fotball", "monkey", "charlie", "aa123456", "donald"};
            for (String easyPasswd : easyToGuessWords) {
                if (checkWord(salt, easyPasswd, passwdLine)){
                    
                if (easyPasswd.length() > 8)
                        easyPasswd = easyPasswd.substring(0, 8);
                System.out.println(easyPasswd);
                foundPasswords.add(easyPasswd);
                break;
                }
            }

        }

        int userIndex = 0;

        // No mangles
        for (String passwdLine : passwdLines) {

            String salt = passwdLine.split(":")[1].substring(0, 2);

            // For each line in the password file
            for (String word : dictWords) {

                if (word.length() > 8)
                    word = word.substring(0, 8);
                
                // If the hashed word matches the password hash
                if (checkWord(salt, word, passwdLine)) {
                    System.out.println(word);
                    foundPasswords.add(word);
                    usersDone.add(userIndex);
                    break;
                }
            }
            userIndex++;
        }

        userIndex = 0;

        // one mangle
        users:
        for (String passwdLine : passwdLines) {

            if(usersDone.contains(userIndex)){
                    userIndex++;
                    continue;
            }

            String salt = passwdLine.split(":")[1].substring(0, 2);

            for (String word : dictWords) {

                String upperCaseWord = word.toUpperCase();

                if (upperCaseWord.length() > 8)
                    upperCaseWord = upperCaseWord.substring(0, 8);

                if (checkWord(salt, upperCaseWord, passwdLine)) {
                    System.out.println(upperCaseWord);
                    foundPasswords.add(upperCaseWord);
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                String lowerCaseWord = word.toLowerCase();

                if (lowerCaseWord.length() > 8)
                    lowerCaseWord = lowerCaseWord.substring(0, 8);
                
                if (checkWord(salt, lowerCaseWord, passwdLine)) {
                    System.out.println(lowerCaseWord);
                    foundPasswords.add(lowerCaseWord);
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
                
                if(deleteFirstCharacterMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
                
                if(deleteLastCharacterMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                if(reverseStringMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                if(duplicateStringMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                if(reflectStringEndMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
                
                if(reflectStringStartMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                if(capitalizeMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
                
                if(ncapitalizeMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }

                if(toggleCaseMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
                
                if(toggleCaseStartUpperMangle(salt, word, passwdLine)){
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users;
                }
            }
            userIndex++;
        }

        // Append&Prepend
        userIndex = 0;
        users2:
        for (String passwdLine : passwdLines) {

            if(usersDone.contains(userIndex)){
                userIndex++;
                continue;
            }
    
            for (String word : dictWords) {
                String salt = passwdLine.split(":")[1].substring(0, 2);
    
                if (prependAndAppendMangle(salt, word, passwdLine)){
                    foundPasswords.add(word);
                    usersDone.add(userIndex);
                    userIndex++;
                    continue users2;
                } 
         }
         userIndex++;
        }
    }

    // Function to check if the hashed word matches the password hash
    public static boolean checkWord(String salt, String word, String passwdLine) {
        if (foundPasswords.contains(word))
            return false;
        else
            return jcrypt.crypt(salt, word).equals(passwdLine.split(":")[1]);
    }

    public static boolean prependAndAppendMangle(String salt, String word, String passwdLine) {
        for (char c = '0'; c <= '9'; ++c) {
            String prependMangle = c + word;
            String appendMangle = word + c;
                        
            if (prependMangle.length() > 8)
                prependMangle = prependMangle.substring(0, 8);

            if (appendMangle.length() > 8)
                appendMangle = appendMangle.substring(0, 8);

            if (checkWord(salt, prependMangle, passwdLine)) {
                System.out.println(prependMangle);
                foundPasswords.add(prependMangle);
                return true;
            }
            if (checkWord(salt, appendMangle, passwdLine)) {
                System.out.println(appendMangle);
                foundPasswords.add(appendMangle);
                return true;
            }
        }
        for (char c = 'a'; c <= 'z'; ++c) {
            String prependMangle = c + word;
            String appendMangle = word + c;
            String prependMangleUpper = Character.toUpperCase(c) + word;
            String appendMangleUpper = word + Character.toUpperCase(c);

        if (prependMangle.length() > 8)
            prependMangle = prependMangle.substring(0, 8);

        if (appendMangle.length() > 8)
            appendMangle = appendMangle.substring(0, 8);

        if (appendMangleUpper.length() > 8)
            appendMangleUpper = appendMangleUpper.substring(0, 8);

        if (appendMangle.length() > 8)
            appendMangle = appendMangle.substring(0, 8);

            if (checkWord(salt, prependMangle, passwdLine)) {
                System.out.println(prependMangle);
                foundPasswords.add(prependMangle);
                return true;
            }
            if (checkWord(salt, appendMangle, passwdLine)) {
                System.out.println(appendMangle);
                foundPasswords.add(appendMangle);
                return true;
            }


            if (checkWord(salt, appendMangleUpper, passwdLine)) {
                System.out.println(appendMangleUpper);
                foundPasswords.add(appendMangleUpper);
                return true;
            }
            if (checkWord(salt, prependMangleUpper, passwdLine)) {
                System.out.println(prependMangleUpper);
                foundPasswords.add(prependMangleUpper);
                return true;
            }
        }
        return false;
    }

    public static boolean deleteFirstCharacterMangle(String salt, String word, String passwdLine) {
        if (word.length() > 1) {
            String mangledWord = word.substring(1);
            
            if (mangledWord.length() > 8)
                mangledWord = mangledWord.substring(0, 8);

            if (checkWord(salt, mangledWord, passwdLine)) {
                System.out.println(mangledWord);
                foundPasswords.add(mangledWord);
                return true;
            }
        }
        return false;
    }
    
    public static boolean deleteLastCharacterMangle(String salt, String word, String passwdLine) {
        if (word.length() > 1) {
            String mangledWord = word.substring(0, word.length() - 1); 

            if (mangledWord.length() > 8)
                mangledWord = mangledWord.substring(0, 8);

            if (checkWord(salt, mangledWord, passwdLine)) {
                System.out.println(mangledWord);
                foundPasswords.add(mangledWord);
                return true;
            }
        }
        return false;
    }

    public static boolean reverseStringMangle(String salt, String word, String passwdLine) {
        String mangledWord = new StringBuilder(word).reverse().toString();

        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);

        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
    }
    
    public static boolean duplicateStringMangle(String salt, String word, String passwdLine) {
        String mangledWord = word + word;

        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);

        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
    }
    
    public static boolean reflectStringStartMangle(String salt, String word, String passwdLine) {
        String mangledWord = word + new StringBuilder(word).reverse().toString();
        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);

        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
    }
    
    public static boolean reflectStringEndMangle(String salt, String word, String passwdLine) {
        String mangledWord = new StringBuilder(word).reverse().toString() + word;
        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);
        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
    }

    public static boolean capitalizeMangle(String salt, String word, String passwdLine) {

        if(word.length() == 1){
            if (checkWord(salt, word.toUpperCase(), passwdLine))
            System.out.println(word);
            foundPasswords.add(word);
            return true;
        }


        String mangledWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);
        if (checkWord(salt, mangledWord, passwdLine)) {
                System.out.println(mangledWord);
                foundPasswords.add(mangledWord);
                return true;
        }
        return false;
    }
    
    public static boolean ncapitalizeMangle(String salt, String word, String passwdLine) {

        if(word.length() == 1){
            if (checkWord(salt, word.toLowerCase(), passwdLine))
                System.out.println(word);
                foundPasswords.add(word);
                return true;
        }

        String mangledWord = word.substring(0, 1).toLowerCase() + word.substring(1).toUpperCase();
        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);

        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
    }
    
    public static boolean toggleCaseMangle(String salt, String word, String passwdLine) {
        StringBuilder mangledWordBuilder = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (i % 2 == 0) {
                mangledWordBuilder.append(Character.toUpperCase(c));
            } else {
                mangledWordBuilder.append(Character.toLowerCase(c));
            }
        }
        String mangledWord = mangledWordBuilder.toString();
    
        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);
    
        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }
        return false;
        }

    public static boolean toggleCaseStartLowerMangle(String salt, String word, String passwdLine) {
    StringBuilder mangledWordBuilder = new StringBuilder();
    for (int i = 0; i < word.length(); i++) {
        char c = word.charAt(i);
        if (i % 2 == 0) {
            mangledWordBuilder.append(Character.toUpperCase(c));
        } else {
            mangledWordBuilder.append(Character.toLowerCase(c));
        }
    }
    String mangledWord = mangledWordBuilder.toString();

    if (mangledWord.length() > 8)
        mangledWord = mangledWord.substring(0, 8);

    if (checkWord(salt, mangledWord, passwdLine)) {
        System.out.println(mangledWord);
        foundPasswords.add(mangledWord);
        return true;
    }
    return false;
    }

    public static boolean toggleCaseStartUpperMangle(String salt, String word, String passwdLine) {
        StringBuilder mangledWordBuilder = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (i % 2 == 1) {
                mangledWordBuilder.append(Character.toUpperCase(c));
            } else {
                mangledWordBuilder.append(Character.toLowerCase(c));
            }
        }
        String mangledWord = mangledWordBuilder.toString();

        if (mangledWord.length() > 8)
            mangledWord = mangledWord.substring(0, 8);

        if (checkWord(salt, mangledWord, passwdLine)) {
            System.out.println(mangledWord);
            foundPasswords.add(mangledWord);
            return true;
        }

        return false;
    }

}