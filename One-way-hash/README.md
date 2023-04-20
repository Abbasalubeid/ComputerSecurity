## CollisionResistance

This program generates random strings and computes their hash values until a collision is found with the hash of the provided messages. To run the program, follow these steps:

1. Open a terminal or command prompt.
2. Navigate to the directory where the `CollisionResistance.java` file is saved.
3. Compile the program by entering the following command: ```javac CollisionResistance.java```
4. Run the program by entering the following command: ```java CollisionResistance```
5. The program will output the messages and the number of trials required to find a collision for each message.
6. The program does not require any arguments to be passed.

## BitCounter

This program compares two MD5 and two SHA-256 hash values and counts the number of bits that are the same between each pair of hashes. To run the program, follow these steps:

1. Open a terminal or command prompt.
2. Navigate to the directory where the `BitCounter.java` file is saved.
3. Compile the program by entering the following command: ```javac BitCounter.java```
4. Run the program by entering the following command, along with the four hash values as arguments: ```java BitCounter <MD5 Hash 1> <MD5 Hash 2> <SHA-256 Hash 1> <SHA-256 Hash 2>```
5. The program will output the number of bits that are the same between each pair of hashes.
6. Make sure to replace `<MD5 Hash 1>`, `<MD5 Hash 2>`, `<SHA-256 Hash 1>`, and `<SHA-256 Hash 2>` with the actual hash values you want to compare when running the program.
