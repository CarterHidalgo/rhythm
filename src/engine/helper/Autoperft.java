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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import engine.model.Model;

public class Autoperft {
    private static final Path teacherPath = Paths.get("src", "resources", "stockfish.exe");
    private static final Path perftsuitePath = Paths.get("src", "resources", "perftsuite.epd");
    private static List<String> tests = new ArrayList<>();
    private static Process process;
    private static BufferedWriter writer;
    private static BufferedReader reader;
    private static ConcurrentLinkedQueue<String> output = new ConcurrentLinkedQueue<>();

    private static final Pattern patternNodes = Pattern.compile("Nodes searched: \\d+");
    private static final Pattern patternMoves = Pattern.compile("[a-h][1-8][a-h][1-8][qrbn]?: (\\d+)");

    public static final boolean test(String mode) {
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

            System.out.println("FEN: " + parts[0]);

            for (int depth = 1; depth < parts.length; depth++) {
                Model.setPosition(parts[0]);
                long studentNodes = Model.perft(depth, false);
                long teacherNodes = Long.valueOf(parts[depth].split(" ")[1]);
                
                System.out.printf("%-10s%15d ", " Depth: " + depth, studentNodes);
                
                if (studentNodes == teacherNodes) {
                    Printer.green("PASS");
                    System.out.println();
                } else {
                    Printer.red("FAIL");
                    System.out.println(", expected " + teacherNodes);

                    boolean longMode = mode.equals("long");
                    bisect(depth, parts[0], longMode);
                    closeProcess();

                    return false;
                }
            }
        }

        Printer.green("All tests passed\n");

        return true;
    }

    public static final boolean test(FEN fen) {
        System.out.println("[TODO]: autoperft with fen " + fen);
        return false;
    }

    public static final void bisect(int depth, boolean longMode) {
        if (!openProcess(teacherPath.toString())) {
            System.out.println("Unable to open teacher executable at path \"" + teacherPath + "\"");

            return;
        }

        String fenStr = FEN.create().toString();

        bisect(depth, fenStr, longMode);
        closeProcess();

        Model.setPosition(fenStr);
    }

    private static final void bisect(int depth, String fenStr, boolean longMode) {
        bisect(depth, fenStr, new ArrayList<String>(), longMode, true);
    }
    
    private static final void bisect(int depth, String fenStr, ArrayList<String> moves, boolean longMode, boolean root) {
        if(root) {
            System.out.println("\nBisecting in " + (longMode ? "long" : "normal") + " mode...");
        }
        
        Map<String, Long> teacherMap = getTeacherPerftMap(depth, fenStr, moves);
        Map<String, Long> studentMap = Model.getPerftMap(depth, fenStr, moves);

        Set<String> extra = new HashSet<>(studentMap.keySet());
        extra.removeAll(teacherMap.keySet());

        Set<String> missing = new HashSet<>(teacherMap.keySet());
        missing.removeAll(studentMap.keySet());

        String indent = " " + "  ".repeat(moves.size());

        for(Map.Entry<String, Long> entry : teacherMap.entrySet()) {
            String key = entry.getKey();
            Long student = studentMap.get(key);
            
            if(student != null) {
                long teacher = entry.getValue();
                long diff = Math.abs(student - teacher);
                
                if(diff != 0) {
                    moves.add(key);

                    System.out.print(indent + key + " (");
                    Printer.red(student);
                    System.out.print(" != ");
                    Printer.green(teacher);
                    System.out.println(")");

                    if(depth > 1) {
                        bisect(depth - 1, fenStr, moves, longMode, false);
                    }

                    moves.removeLast();
                    
                    if(!longMode) {
                        break;
                    }
                }
            }
        }

        printMissing(missing);
        printExtra(extra);
    }

    private static final void printMissing(Set<String> missing) {
        int count = 0;

        for(String str : missing) {
            Printer.red("- " + str + " missing" + ((count < 9) ? "\n" : ""));

            if(++count > 9) {
                System.out.println(" ... and " + (missing.size() - 10) + " more\n");
                return;
            }
        }
    }

    private static final void printExtra(Set<String> extra) {
        int count = 0;

        for(String str : extra) {
            Printer.green("- " + str + " extra" + ((count < 9) ? "\n" : ""));

            if(++count > 9) {
                System.out.println(" ... and " + (extra.size() - 10) + " more\n");
                return;
            }
        }
    }

    private static final Map<String, Long> getTeacherPerftMap(int depth, String fenStr, ArrayList<String> moves) {
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

    private static final List<String> waitForMatch(Pattern regex, int timeout) {
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

    private static final void sendCommand(String cmd) {
        try {
            writer.write(cmd + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final boolean openProcess(String path) {
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

    private static final void closeProcess() {
        try {
            writer.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
