// 231RDB376, Aleksandrs Belkins, 6. grupa
// 231RDB340, Lauris Limanovičs, 6. grupa 
// 231RDB378, Ksenija Šitikova, 6. grupa

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String choiceStr;
        String sourceFile, resultFile, firstFile, secondFile;

        System.out.println("List of commands:");
        System.out.println("(1) comp - Compress a file");
        System.out.println("(2) decomp - Decompress a file");
        System.out.println("(3) size - Check the size of a file");
        System.out.println("(4) equal - Check if two files are equal");
        System.out.println("(5) about - About the authors");
        System.out.println("(6) exit - Exit the program");

        while (true) {
            System.out.println("Choose a command: ");
            choiceStr = sc.next();

            switch (choiceStr) {
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
                    System.out.println("Compression rate: " + compressionRate);
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
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid command, please choose again.");
            }
        }
    }

    public static void comp(String sourceFile, String resultFile) {
        try (FileInputStream inputFile = new FileInputStream(sourceFile);
             FileOutputStream outputFile = new FileOutputStream(resultFile)) {

            final int windowSize = 8192;
            final int bufferSize = windowSize + 1024 * 1024;
            byte[] buffer = new byte[bufferSize];
            int bufferEnd = inputFile.read(buffer);

            int pos = 0;
            while (pos < bufferEnd) {
                int controlByte = 0;
                int controlBit = 0;
                ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

                while (controlBit < 8 && pos < bufferEnd) {
                    int maxMatchDistance = Math.min(pos, windowSize);
                    int maxMatchLength = 0;
                    int bestMatchDistance = -1;

                    // Find the longest match
                    for (int i = pos - maxMatchDistance; i < pos; i++) {
                        int matchLength = 0;
                        while (matchLength < bufferEnd - pos && buffer[i + matchLength] == buffer[pos + matchLength]) {
                            matchLength++;
                            if (matchLength == 255) break; // Limit match length to 255
                        }

                        if (matchLength > maxMatchLength) {
                            maxMatchLength = matchLength;
                            bestMatchDistance = pos - i;
                        }
                    }

                    if (maxMatchLength >= 3) {
                        controlByte |= (1 << (7 - controlBit));
                        tempStream.write((bestMatchDistance >> 8) & 0xFF);
                        tempStream.write(bestMatchDistance & 0xFF);
                        tempStream.write(maxMatchLength);
                        pos += maxMatchLength;
                    } else {
                        tempStream.write(buffer[pos]);
                        pos++;
                    }
                    controlBit++;
                }

                outputFile.write(controlByte);
                outputFile.write(tempStream.toByteArray());

                if (pos >= bufferEnd - 1024 && bufferEnd != bufferSize) {
                    int remaining = bufferEnd - pos;
                    System.arraycopy(buffer, pos, buffer, 0, remaining);
                    int bytesRead = inputFile.read(buffer, remaining, bufferSize - remaining);
                    if (bytesRead > 0) {
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

    public static void decomp(String compressedFile, String outputFile) {
        try (FileInputStream fis = new FileInputStream(compressedFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedInputStream inputFile = new BufferedInputStream(fis);
            BufferedOutputStream outputStream = new BufferedOutputStream(fos)) {

            ArrayList<Byte> history = new ArrayList<>();

            while (inputFile.available() > 0) {
                int controlByte = inputFile.read();
                if (controlByte == -1) break;

                for (int i = 0; i < 8 && inputFile.available() > 0; i++) {
                    if ((controlByte & (1 << (7 - i))) != 0) {
                        if (inputFile.available() < 3) break;

                        int distance = inputFile.read() << 8 | inputFile.read(); 
                        int length = inputFile.read(); 

                        if (distance == 0 || length == 0) break; 

                        int start = history.size() - distance; 
                        for (int j = 0; j < length; j++) {
                            byte b = history.get(start + j % distance); 
                            history.add(b);
                            outputStream.write(b);
                        }
                    } else { // Literal byte
                        int literal = inputFile.read();
                        if (literal == -1) break; 
                        history.add((byte) literal);
                        outputStream.write(literal);
                    }
                }
            }
            outputStream.flush();
        } catch (IOException ex) {
            System.out.println("Error during decompression: " + ex.getMessage());
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
            } while (!(k1 == -1 && k2 == -1));
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
        if (originalSize == 0) return 0; 
    
        double compressionRate = (1 - (compressedSize / originalSize));
        return compressionRate; 
    }  
}
