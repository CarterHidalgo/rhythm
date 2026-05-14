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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import engine.model.Model;

public class Autoperft {
    private static final Path teacherPath = Paths.get("stockfish.exe");
    private static final Path perftsuitePath = Paths.get("src", "resources", "perftsuite.epd");
    private static List<String> tests = new ArrayList<>();
    private static Process process;
    private static BufferedWriter writer;
    private static BufferedReader reader;
    private static ConcurrentLinkedQueue<String> output = new ConcurrentLinkedQueue<>();

    private static final Pattern patternNodes = Pattern.compile("Nodes searched: \\d+");
    private static final Pattern patternMoves = Pattern.compile("[a-h][1-8][a-h][1-8][qrbn]?: (\\d+)");

    public static boolean test(String mode) {
        if (!openProcess(teacherPath.toString())) {
            System.out.println("Unable to open teacher executable at path \"" + teacherPath + "\"");
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
                Model.setPosition(parts[0], null);
                long studentNodes = Model.perft(depth, false);
                long teacherNodes = Long.valueOf(parts[depth].split(" ")[1]);
                
                System.out.printf("%-10s%15d ", "Depth: " + depth, studentNodes);
                
                if (studentNodes == teacherNodes) {
                    Printer.green("PASS");
                    System.out.println();
                } else {
                    Printer.red("FAIL");
                    System.out.println(", expected " + teacherNodes);

                    bisect(depth, parts[0], mode);
                    closeProcess();

                    return false;
                }
            }
        }

        Printer.green("All tests passed\n");

        return true;
    }

    public static boolean test(FEN fen) {
        System.out.println("[TODO]: autoperft with fen " + fen);
        return false;
    }

    private static void bisect(int depth, String fenStr, String mode) {
        System.out.println("\nBisecting in " + (mode.equals("long") ? "long" : "normal") + " mode...");
        bisect(depth, depth, fenStr, mode, new ArrayList<String>());
    }

    private static void bisect(int depth, int totalDepth, String fenStr, String mode, List<String> moves) {
        for(int d = depth; d > 0; d--) {
            Map<String, Long> teacherPerftMap = getTeacherPerftMap(d, fenStr, new ArrayList<String>(moves));
            Map<String, Long> studentPerftMap = Model.getPerftMap(d, fenStr, new ArrayList<String>(moves));

            int count = 0;
            for (String key : studentPerftMap.keySet()) {
                if (!teacherPerftMap.containsKey(key)) {
                    if(count < 10) {
                        Printer.green("  ".repeat(moves.size()) + "  - " + key + " extra" + ((count < 9) ? "\n" : ""));
                    }

                    count++;
                }
            }

            if(count > 10) {
                System.out.println(" ... and " + (count - 10) + " more");
            }

            int minDiff = Integer.MAX_VALUE;
            int faultyMoves = 0;
            String move = "";
            
            for (String key : teacherPerftMap.keySet()) {
                int diff = Math.abs(studentPerftMap.get(key).intValue() - teacherPerftMap.get(key).intValue());
                if (studentPerftMap.containsKey(key) && diff != 0) {
                    faultyMoves++;

                    if(mode.equals("long")) {
                        moves.add(key);
                        
                        System.out.print("  ".repeat(moves.size()) + (totalDepth - depth + 1) + ". " + key + " (");
                        Printer.red(String.valueOf(studentPerftMap.get(key)));
                        System.out.print(" != ");
                        Printer.green(String.valueOf(teacherPerftMap.get(key)));
                        System.out.println(")");
                        
                        bisect(d - 1, totalDepth, fenStr, mode, moves);
                        
                        moves.removeLast();
                    } else if(diff < minDiff) {
                        minDiff = diff;
                        move = key;
                    }
                }
                
                count = 0;
                if (!studentPerftMap.containsKey(key)) {
                    if(count < 10) {
                        Printer.red("  ".repeat(moves.size()) + "  - " + key + " missing" + ((count < 9) ? "\n" : ""));
                    }

                    count++;
                }

                if (count > 10) {
                    System.out.println(" ... and " + (count - 10) + " more");
                }
            }

            if(!move.isEmpty()) {
                moves.add(move);

                System.out.print("  ".repeat(moves.size()) + (totalDepth - d + 1) + ". " + move + " (");
                Printer.red(String.valueOf(studentPerftMap.get(move)));
                System.out.print(" != ");
                Printer.green(String.valueOf(teacherPerftMap.get(move)));
                System.out.println(") out of " + faultyMoves + " faulty");
            }
        }
    }

    private static Map<String, Long> getTeacherPerftMap(int depth, String fenStr, List<String> moves) {
        StringBuilder cmd = new StringBuilder();
        Map<String, Long> map = new HashMap<>();

        if (fenStr.isBlank()) {
            cmd.append("position startpos");
        } else {
            cmd.append("position fen " + fenStr);
        }

        if (moves != null && moves.size() > 0) {
            cmd.append(" moves " + String.join(" ", moves));
        }

        cmd.append("\ngo perft " + depth);

        sendCommand(cmd.toString());
        for (String line : waitForMatch(patternNodes, 5000)) {
            if (patternMoves.matcher(line).matches()) {
                String[] parts = line.split(": ");
                map.put(parts[0], Long.valueOf(parts[1]));
            }
        }

        return map;
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
                    if (line != null) {
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
            ProcessBuilder processBuilder = new ProcessBuilder(path.toString()).redirectErrorStream(true);
            process = processBuilder.start();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
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
