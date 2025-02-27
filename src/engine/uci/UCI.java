/*
 * Author: FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI)
 * 
 * Purpose: Starts and manages UCI threads, inputs, and outputs; interacts with the model via
 * UciListener listener
 */

package engine.uci;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.stream.Collectors;

import engine.helper.Autoperft;
import engine.helper.FEN;
import engine.helper.Printer;
import engine.model.GameInfo;
import engine.model.Model;

public class UCI {
    private static final Map<String, Consumer<String>> commands = new HashMap<>();

    private static PrintStream out = System.out;
    private static Scanner in = new Scanner(System.in);

    private static final Pattern patternWhiteTime = Pattern.compile(".*wtime (\\d+).*");
    private static final Pattern patternWhiteTimeIncr = Pattern.compile(".*winc (\\d+).*");
    private static final Pattern patternBlackTime = Pattern.compile(".*btime (\\d+).*");
    private static final Pattern patternBlackTimeIncr = Pattern.compile(".*binc (\\d+).*");
    private static final Pattern patternMovesToGo = Pattern.compile(".*movestogo (\\d+).*");
    private static final Pattern patternDepth = Pattern.compile(".*depth (\\d+).*");
    private static final Pattern patternNodes = Pattern.compile(".*nodes (\\d+).*");
    private static final Pattern patternMate = Pattern.compile(".*mate (\\d+) .*");
    private static final Pattern patternMovetime = Pattern.compile(".*movetime (\\d+).*");
    private static final Pattern patternPerft = Pattern.compile("perft ([1-9]|[1-9]\\\\d).*");
    private static final Pattern positionPattern =
            Pattern.compile("(?:fen (?<fen>.* \\d+ \\d+)|startpos)(?: moves (?<moves>.*))?");

    private static final Pattern[] goNumeralPatterns = new Pattern[] {patternWhiteTime,
            patternWhiteTimeIncr, patternBlackTime, patternBlackTimeIncr, patternMovesToGo,
            patternDepth, patternNodes, patternMate, patternMovetime, patternPerft};

    private static final String name = "Rhythm";
    private static final String author = "Carter Hidalgo";

    private static Thread uciThread;

    static {
        commands.put("uci", UCI::uci);
        commands.put("setoption", UCI::setOption);
        commands.put("ucinewgame", UCI::uciNewGame);
        commands.put("isready", UCI::isReady);
        commands.put("position", UCI::position);
        commands.put("go", UCI::go);
        commands.put("print", UCI::print);
        commands.put("info", UCI::info);
        commands.put("autoperft", UCI::autoperft);
        commands.put("help", UCI::help);
        commands.put("quit", UCI::quit);
    }

    public static void start() {
        uciThread = new Thread(UCI::run);
        uciThread.start();
    }

    private static void run() {
        while (Model.isRunning()) {
            String message = in.nextLine();
            String[] commandAndParams = message.split(" ", 2);
            String command = commandAndParams[0];
            String params = commandAndParams.length > 1 ? commandAndParams[1] : "";

            if (commands.containsKey(command)) {
                commands.get(command).accept(params);
            } else {
                out.println("Unknown command \"" + command + "\". Type help for more information.");
            }
        }
    }

    private static void uci(String ignore) {
        out.println("id name " + name);
        out.println("id author " + author);

        LinkedList<String> options = new LinkedList<>();
        for (String option : options) {
            out.println("option " + option);
        }

        out.println("uciok");
    }

    private static void setOption(String option) {
        System.out.println("[TODO]: setOptions");
    }

    private static void uciNewGame(String ignore) {
        System.out.println("[TODO]: cleanup for a new game");
    }

    private static void isReady(String ignore) {
        synchronized (Model.getLock()) {
            while (!Model.getReady()) {
                try {
                    Model.getLock().wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        out.println("readyok");
    }

    private static void position(String position) {
        String[] moves;
        String startingPosition = FEN.START_FEN;
        Matcher matcher = positionPattern.matcher(position);

        if (matcher.matches()) {
            if (matcher.group("fen") != null) {
                startingPosition = matcher.group("fen");
            }

            if (matcher.group("moves") != null) {
                moves = matcher.group("moves").split(" ");
            }
        }

        System.out.println("[TODO]: set position with moves");
    }

    private static void go(String params) {
        List<Optional<Integer>> options = Arrays.stream(goNumeralPatterns).map(pattern -> {
            Matcher matcher = pattern.matcher(params);
            Optional<Integer> option = Optional.empty();

            if (matcher.matches()) {
                option = Optional.of(Integer.parseInt(matcher.group(1)));
            }

            return option;
        }).collect(Collectors.toList());

        Boolean infinite = params.contains("infinite");

        GoParameters goParameters = new GoParameters(options.get(0), options.get(1), options.get(2),
                options.get(3), options.get(4), options.get(5), options.get(6), options.get(7),
                options.get(8), options.get(9), infinite);

        if (goParameters.getPerft().isPresent()) {
            if (goParameters.getPerft().get() < 1) {
                return;
            }

            long start = System.nanoTime();
            int nodes = Model.perft(goParameters.getPerft().get(), true);
            long taken = System.nanoTime() - start;

            if (nodes > 0) {
                out.println("\nnodes: " + nodes);
                out.println("time: " + taken / 1_000_000 + "ms");
                out.println("nps: " + (int) (nodes / (++taken / 1_000_000_000.0)) + "\n");
            }

            return;
        } else {
            new Thread(() -> {
                System.out.println("[TODO]: go find best move");
            }).start();
        }

    }

    private static void print(String param) {
        if (param.isEmpty()) {
            Printer.board();
        } else {
            Printer.bitboard(param);
        }
    }

    private static void info(String ignore) {
        out.println("\nTurn: " + (GameInfo.getTurn() ? "white" : "black") + "\nHalfmoves: "
                + GameInfo.getHalfmoves() + "\nFullmoves: " + GameInfo.getFullmoves()
                + "\nCastling: " + GameInfo.getCastlingString() + "\nSide: "
                + GameInfo.getSideString() + "\n");
    }

    private static void autoperft(String param) {
        if (param.equals("")) {
            Autoperft.test();
        } else {
            FEN fen = new FEN(param);
            Autoperft.test(fen);
        }
    }

    private static void help(String ignore) {
        out.println("[TODO]: help");
    }

    private static void quit(String ignore) {
        Model.setRunning(false);
    }
}
