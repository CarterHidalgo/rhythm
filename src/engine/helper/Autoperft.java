/*
 * Name: Autoperft.java Author: Pigpen (based on code from sohamkorade:
 * https://github.com/sohamkorade/autoperft)
 * 
 * Purpose: Run automatic perft tests and bisect missing or extra moves
 */

package engine.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import engine.model.Model;

public class Autoperft {
    private static final Path teacherPath = Paths.get("src", "resources", "stockfish.exe");
    private static final Path perftsuitePath = Paths.get("src", "resources", "perftsuite.epd");
    private static ProcessBuilder processBuilder =
            new ProcessBuilder(teacherPath.toString()).redirectErrorStream(true);
    private static List<String> output = new ArrayList<>();
    private static List<String> tests = new ArrayList<>();
    private static Process process;
    private static BufferedWriter writer;
    private static BufferedReader reader;
    private static Scanner input;

    public static boolean test() {
        openProcess();
        loadTests();

        outer: for (int i = 0; i < tests.size(); i++) {
            System.out.println("Test " + (i + 1) + "/" + tests.size());
            String[] parts = tests.get(i).split(" ;");

            System.out.println(" FEN: " + parts[0]);

            for (int depth = 1; depth < parts.length; depth++) {
                System.out.print(" Depth: " + depth + " ".repeat(15));

                Model.setPosition(parts[0]);
                long student = Model.perft(depth, false);
                long teacher = Long.valueOf(parts[depth].split(" ")[1]);

                if (student == teacher) {
                    Printer.green("PASS");
                    System.out.println();
                } else {
                    Printer.red("FAIL");
                    System.out.println(", expected " + teacher + " but found " + student);
                    // bisect(depth, parts[0]);
                    break outer;
                }
            }
        }

        closeProcess();

        return false;
    }

    public static boolean test(FEN fen) {
        return false;
    }

    private static void bisect(int depth, String fen) {
        System.out.println("  Bisecting...");

    }

    private static void run(String cmd) {
        try {
            writer.write(cmd + "\n");
            writer.flush();

            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                output.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openProcess() {
        try {
            process = processBuilder.start();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            input = new Scanner(perftsuitePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeProcess() {
        try {
            writer.close();
            reader.close();
            input.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadTests() {
        while (input.hasNextLine()) {
            tests.add(input.nextLine());
        }

        System.out.println("\n" + tests.size() + " tests loaded\n");
    }
}
