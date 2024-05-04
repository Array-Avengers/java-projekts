// 231RDB376, Aleksandrs Belkins, 6. grupa
// 231RDB340, Lauris Limanovičs, 6. grupa 
// 231RDB378, Ksenija Šitikova, 6. grupa

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String choiseStr;
        String sourceFile, resultFile, firstFile, secondFile;

        System.out.println("List of commands:");
        System.out.println("(1) comp - Compress a file");
        System.out.println("(2) decomp - Decompress a file");
        System.out.println("(3) size - Check the size of a file");
        System.out.println("(4) equal - Check if two files are equal");
        System.out.println("(5) about - About the authors");
        System.out.println("(6) exit - Exit the program");

        loop: while (true) {

            System.out.println("Choose a command: ");
            choiseStr = sc.next();

            switch (choiseStr) {
                case "comp":
                    System.out.print("source file name: ");
                    sourceFile = sc.next();
                    System.out.print("archive name: ");
                    resultFile = sc.next();
                    comp(sourceFile, resultFile);
                    break;
                case "decomp":
                    System.out.print("archive name: ");
                    sourceFile = sc.next();
                    System.out.print("file name: ");
                    resultFile = sc.next();
                    decomp(sourceFile, resultFile);
                    break;
                case "size":
                    System.out.print("file name: ");
                    sourceFile = sc.next();
                    size(sourceFile);
                    break;
                case "equal":
                    System.out.print("first file name: ");
                    firstFile = sc.next();
                    System.out.print("second file name: ");
                    secondFile = sc.next();
                    System.out.println(equal(firstFile, secondFile));
                    break;
                case "about":
                    about();
                    break;
                case "exit":
                    break loop;
            }
        }

        sc.close();
    }

    public static void comp(String sourceFile, String resultFile) {
        try {
            FileInputStream inputFile = new FileInputStream(sourceFile);
            FileOutputStream outputFile = new FileOutputStream(resultFile);

            int windowSize = 1024; // Size of the sliding window
            byte[] buffer = new byte[windowSize]; // Buffer to hold data from the input file

            int bytesRead;
            while ((bytesRead = inputFile.read(buffer)) != -1) {
                int pos = 0;
                while (pos < bytesRead) {
                    // Find the longest match within the sliding window
                    int matchLength = 0;
                    int matchOffset = 0;
                    for (int i = Math.max(0, pos - windowSize); i < pos; i++) {
                        int len = 0;
                        while (pos + len < bytesRead && buffer[i + len] == buffer[pos + len]) {
                            len++;
                        }
                        if (len > matchLength) {
                            matchLength = len;
                            matchOffset = pos - i;
                        }
                    }

                    // Write the LZ77 tuple (offset, length, next_character) to the output file
                    if (matchLength > 0) {
                        outputFile.write(matchOffset); // Offset
                        outputFile.write(matchLength); // Length
                        outputFile.write(buffer[pos + matchLength]); // Next character
                        pos += matchLength + 1;
                    } else {
                        outputFile.write(0); // No match, write zero
                        outputFile.write(0); // Zero length
                        outputFile.write(buffer[pos]); // Next character
                        pos++;
                    }
                }
            }

            inputFile.close();
            outputFile.close();

            System.out.println("Compression completed.");
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    public static void decomp(String sourceFile, String resultFile) {
        // TODO: implement this method
    }

    public static void size(String sourceFile) {
        try {
            FileInputStream f = new FileInputStream(sourceFile);
            System.out.println("size: " + f.available());
            f.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static boolean equal(String firstFile, String secondFile) {
        try {
            FileInputStream f1 = new FileInputStream(firstFile);
            FileInputStream f2 = new FileInputStream(secondFile);
            int k1, k2;
            byte[] buf1 = new byte[1000];
            byte[] buf2 = new byte[1000];
            do {
                k1 = f1.read(buf1);
                k2 = f2.read(buf2);
                if (k1 != k2) {
                    f1.close();
                    f2.close();
                    return false;
                }
                for (int i = 0; i < k1; i++) {
                    if (buf1[i] != buf2[i]) {
                        f1.close();
                        f2.close();
                        return false;
                    }

                }
            } while (!(k1 == -1));
            f1.close();
            f2.close();
            return true;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public static void about() {
        System.out.println("231RDB376, Aleksandrs Belkins, 6. grupa");
        System.out.println("231RDB340, Lauris Limanovičs, 6. grupa");
        System.out.println("231RDB378, Ksenija Šitikova, 6. grupa");
    }

    public static double calculateCompressionRate(String sourceFile, String compressedFile) {
        File source = new File(sourceFile);
        File compressed = new File(compressedFile);
        double originalSize = source.lenght();
        double compressedSize = compressed.length();
        return (1 - (compressedSize / originalSize)) * 100;
    }
}
