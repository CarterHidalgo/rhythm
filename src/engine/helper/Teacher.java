/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Send commands to and receive responses from a teacher engine process for perft bisection
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class Teacher {
    private static final Path TEACHER_PATH = Paths.get("src", "resources", "teacher.exe");
    private static final Pattern PATTERN_NODES = Pattern.compile("Nodes searched: \\d+");
    private static final Pattern PATTERN_MOVES = Pattern.compile("[a-h][1-8][a-h][1-8][qrbn]?: (\\d+)");

    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ConcurrentLinkedQueue<String> output = new ConcurrentLinkedQueue<>();

    private boolean loaded = false;

    public Teacher() {
        openProcess(TEACHER_PATH.toString());

        loaded = true;
    }

    public Teacher(String teacherPath) {
        openProcess(teacherPath);

        loaded = true;
    }

    public final Map<String, Long> getPerftMap(int depth, String fenStr, List<String> moves) {
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
        for (String line : waitForMatch(PATTERN_NODES, 5000)) {
            if (PATTERN_MOVES.matcher(line).matches()) {
                String[] parts = line.split(": ");
                map.put(parts[0], Long.valueOf(parts[1]));
            }
        }

        return map;
    }
    
    public final boolean openProcess(String teacherPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(teacherPath.toString()).redirectErrorStream(true);
            process = processBuilder.start();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unable to open teacher executable at path \"" + teacherPath + "\"");
            
            return false;
        }

        return true;
    }

    public final void closeProcess() {
        try {
            writer.close();
            reader.close();
            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final boolean isLoaded() {
        return loaded;
    }

    private final List<String> waitForMatch(Pattern regex, int timeout) {
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

    private final void sendCommand(String cmd) {
        try {
            writer.write(cmd + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
