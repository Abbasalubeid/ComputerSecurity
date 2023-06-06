# Modes Of Operation
This is a Java application that allows the encryption and decryption of files using two keys in Double AES algorithm. The application supports three modes of operation - CBC (Cipher Block Chaining), CFB (Cipher Feedback), and CTR (Counter) mode.

## Files Included

- `DoubleAesModes.java`: Implements the DoubleAesModes class, which provides a double AES encryption and decryption scheme using different modes of operation.

- `DoubleAes.java`: Contains the main class DoubleAes, which serves as a command-line interface (CLI) for interacting with the DoubleAesModes class.

## How to Compile and Run

1. Ensure you have a Java development environment (JDK) installed on your system.

2. Open a terminal or command prompt and navigate to the project directory.

3. Compile the file DoubleAes.java using the following command:

```bash
javac DoubleAes.java 
```

## Usage

You can use this application from the command line with the following syntax:

java DoubleAes --key1 <key1> --key2 <key2> --input <input file> --output <output file> --method <dec/enc> --mode <cbc/cfb/ctr>


**Parameters**

- `--key1 <key1>`: The first key to be used for the double AES encryption or decryption.
- `--key2 <key2>`: The second key to be used for the double AES encryption or decryption.
- `--input <input file>`: The input file to be encrypted or decrypted. 
- `--output <output file>`: The output file where the result of the encryption or decryption should be written.
- `--method <dec/enc>`: The method to be used. Use 'enc' for encryption and 'dec' for decryption.
- `--mode <cbc/cfb/ctr>`: The mode of operation to be used. Supported modes are CBC (Cipher Block Chaining), CFB (Cipher Feedback), and CTR (Counter).

Please ensure to replace `<key1>`, `<key2>`, `<input file>`, `<output file>`, `<dec/enc>`, and `<cbc/cfb/ctr>` with your actual parameters.

## Example

Here's an example of how to use the DoubleAes application to encrypt a file named 'plain.txt' with keys 'myKey1' and 'myKey2' using CBC mode:
```bash
java DoubleAes --key1=0123456789abcdef --key2=fedcba9876543210 --input=input.txt --method=enc --mode=cbc --output=encCBC.txt
```

And here's an example of how to decrypt the output file that was generated:
```bash
java DoubleAes --key1=0123456789abcdef --key2=fedcba9876543210 --input=encCBC.txt --method=dec --mode=cbc --output=decCBC.txt
```

## Note

The keys you provide will be hashed using SHA-256 to meet the length requirements of the AES algorithm. 