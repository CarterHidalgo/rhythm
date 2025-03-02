/*
 * Author: Carter Hidalgo (based on code from sohamkorade: https://github.com/sohamkorade/autoperft)
 * 
 * Purpose: Run automatic perft tests and bisect missing or extra moves
 */

package engine.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import engine.model.Model;

// autoperft teacher C:\Users\Carter Hidalgo\chess\engines\stockfish\stockfish-windows-x86-64-avx2.exe
public class Autoperft {
    private static final Path perftsuitePath = Paths.get("src", "resources", "perftsuite.epd");
    private static List<String> tests = new ArrayList<>();
    private static Process process;
    private static BufferedWriter writer;
    private static BufferedReader reader;
    private static ConcurrentLinkedQueue<String> output = new ConcurrentLinkedQueue<>();
    
    private static final Pattern patternNodes = Pattern.compile("Nodes searched: \\d+");
    private static final Pattern patternMoves = Pattern.compile("[a-h][1-8][a-h][1-8][qrbn]?: (\\d+)");

    public static boolean test(String path) {
        if (!path.isEmpty() && !openProcess(path)) {
            System.out.println("Unable to open teacher executable at path \"" + path + "\"");
            return false;
        }

        try (Scanner input = new Scanner(perftsuitePath)) {
            tests.clear();
            
            while (input.hasNextLine()) {
                tests.add(input.nextLine());
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        System.out.println("\n" + tests.size() + " tests loaded\n");

        for (int i = 0; i < tests.size(); i++) {
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

                    if (!path.isEmpty()) {
                        bisect(depth, parts[0]);
                        closeProcess();
                    }

                    return false;
                }
            }
        }

        return true;
    }

    public static boolean test(FEN fen) {
        System.out.println("[TODO]: autoperft with fen " + fen);
        return false;
    }

    private static void bisect(int depth, String fen) {
        System.out.println("  Bisecting...");

        List<String> moves = new ArrayList<>();

        for(int d = depth; d > 0; d--) {
            List<String> teacherMoves = getTeacherPerft(d, fen, moves);
            System.out.println(teacherMoves);
        }
    }

    private static List<String> getTeacherPerft(int depth, String fen, List<String> moves) {
        StringBuilder cmd = new StringBuilder();
        List<String> perft = new ArrayList<>();
        
        if(fen.isBlank()) {
            cmd.append("position startpos");
        } else {
            cmd.append("position fen " + fen);
        }

        if(moves.size() > 0) {
            cmd.append("moves" + String.join(" ", moves));
        }

        cmd.append("\ngo perft " + depth);

        sendCommand(cmd.toString());
        for(String line : waitForMatch(patternNodes, 5000)) {
            if(patternMoves.matcher(line).matches()) {
                perft.add(line);
            }
        }

        return perft;
    }

    private static List<String> waitForMatch(Pattern regex, int timeout) {
        List<String> result = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        boolean matched = false;

        Thread outputThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    if (!reader.ready()) {
                        try {
                            Thread.sleep(1);
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    
                    String line = reader.readLine();
                    if(line != null) {
                        output.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        outputThread.start();

        while (!matched && (System.currentTimeMillis() - startTime) < timeout) {
            if (output.peek() != null) {
                String line = output.poll();
                result.add(line);
                matched = regex.matcher(line).matches();
            }
        }

        try {
            outputThread.interrupt();
            outputThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    private static void sendCommand(String cmd) {
        try {
            writer.write(cmd + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    private static boolean openProcess(String path) {
        try {
            ProcessBuilder processBuilder =
                    new ProcessBuilder(path.toString()).redirectErrorStream(true);
            process = processBuilder.start();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unable to open teacher executable at \"" + path + "\"");

            return false;
        }

        return true;
    }

    private static void closeProcess() {
        try {
            writer.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
