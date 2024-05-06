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

                    long startTime = System.currentTimeMillis();
                    comp(sourceFile, resultFile);
                    long endTime = System.currentTimeMillis();
                    long compressionTime = endTime - startTime;
                    double compressionRate = calculateCompressionRate(sourceFile, resultFile);
                    System.out.println("Compression completed in " + compressionTime + "ms");
                    System.out.println("Compression rate: " + compressionRate + "%");
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
                default:
                    System.out.println("Invalid command, Please choose again.");
            }
        }

        sc.close();
    }

    public static void comp(String sourceFile, String resultFile) {
        try (FileInputStream inputFile = new FileInputStream(sourceFile);
             FileOutputStream outputFile = new FileOutputStream(resultFile)) {

            final int windowSize = 4096;
            final int lookAheadBufferSize = 1024;
            final int MIN_MATCH_LENGTH = 4;
            final int bufferSize = windowSize + lookAheadBufferSize;
            byte[] buffer = new byte[bufferSize];
            int bufferEnd = 0;

            int bytesRead = inputFile.read(buffer);
            bufferEnd = bytesRead;

            int pos = 0;
            while (pos < bufferEnd) {
                int maxMatchDistance = Math.min(pos, windowSize);
                int maxMatchLength = 0;
                int bestMatchDistance = -1;

                for (int i = pos - maxMatchDistance; i < pos; i++) {
                    int matchLength = 0;
                    while (matchLength < bufferEnd - pos && buffer[i + matchLength] == buffer[pos + matchLength]) {
                        matchLength++;
                        if (matchLength == lookAheadBufferSize) break;
                    }

                    if (matchLength > maxMatchLength) {
                        maxMatchLength = matchLength;
                        bestMatchDistance = pos - i;
                    }
                }

                if (maxMatchLength >= MIN_MATCH_LENGTH) {
                    outputFile.write((bestMatchDistance >> 8) & 0xFF);
                    outputFile.write(bestMatchDistance & 0xFF);
                    outputFile.write((maxMatchLength >> 8) & 0xFF);
                    outputFile.write(maxMatchLength & 0xFF);
                    pos += maxMatchLength;
                } else {
                    outputFile.write(0);
                    outputFile.write(0);
                    outputFile.write(0);
                    outputFile.write(0);
                    outputFile.write(buffer[pos]);
                    pos++;
                }

                if (pos + lookAheadBufferSize >= bufferEnd && bufferEnd < bufferSize) {
                    int remaining = bufferEnd - pos;
                    System.arraycopy(buffer, pos, buffer, 0, remaining);
                    bytesRead = inputFile.read(buffer, remaining, bufferSize - remaining);
                    if (bytesRead != -1) {
                        bufferEnd = remaining + bytesRead;
                    } else {
                        bufferEnd = remaining;
                    }
                    pos = 0;
                }
            }
        } catch (IOException ex) {
            System.out.println("Error during compression: " + ex.getMessage());
        }
    }

    public static void decomp(String sourceFile, String resultFile) {
        try (FileInputStream input = new FileInputStream(sourceFile);
             FileOutputStream output = new FileOutputStream(resultFile);
             ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream()) {
    
            while (true) {
                int offset = input.read();
                if (offset == -1) break;
    
                int length = input.read();
                if (length == -1) break;
    
                int nextChar = input.read();
                if (nextChar == -1) break;
    
                if (length == 0) {
                    byteArrayOutput.write(nextChar);
                } else {
                    byte[] byteArray = byteArrayOutput.toByteArray();
                    int startPosition = byteArrayOutput.size() - offset;
                    if (startPosition < 0 || startPosition + length > byteArray.length) {
                        throw new IOException("Invalid offset or length in compressed data.");
                    }
    
                    for (int i = 0; i < length; i++) {
                        byteArrayOutput.write(byteArray[startPosition + i]);
                    }
                    byteArrayOutput.write(nextChar);
                }
            }
    
            byteArrayOutput.writeTo(output);
            System.out.println("Decompression completed successfully.");
    
        } catch (IOException e) {
            System.out.println("Error during decompression: " + e.getMessage());
        }
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
        double originalSize = source.length();
        double compressedSize = compressed.length();
        double compressionRate = (1 - (compressedSize / originalSize));
        return Math.round(compressionRate);
    }
}
