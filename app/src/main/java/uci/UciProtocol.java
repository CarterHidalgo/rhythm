/*
 * Name: UciProtocol.java
 * Author: FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI) Thanks! =]
 * 
 * Purpose: Starts and manages UCI threads, inputs, and outputs; interacts with the model via UciListener listener
 */


package uci;

import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import model.Model;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

public class UciProtocol {
    public static final String STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final UciListener listener;

    private final Map<String, Consumer<String>> commands = new HashMap<>();

    private static PrintStream out = System.out;
    private Scanner in;

    private final Pattern patternWhiteTime = Pattern.compile(".*wtime (\\d+).*");
    private final Pattern patternWhiteTimeIncr = Pattern.compile(".*winc (\\d+).*");
    private final Pattern patternBlackTime = Pattern.compile(".*btime (\\d+).*");
    private final Pattern patternBlackTimeIncr = Pattern.compile(".*binc (\\d+).*");
    private final Pattern patternMovesToGo = Pattern.compile(".*movestogo (\\d+).*");
    private final Pattern patternDepth = Pattern.compile(".*depth (\\d+).*");
    private final Pattern patternNodes = Pattern.compile(".*nodes (\\d+).*");
    private final Pattern patternMate = Pattern.compile(".*mate (\\d+) .*");
    private final Pattern patternMovetime = Pattern.compile(".*movetime (\\d+).*");
    private final Pattern patternPerft = Pattern.compile(".*perft ([1-9]|[1-9]\\\\d).*");
    private final Pattern positionPattern = Pattern.compile("(?:fen (?<fen>.* \\d+ \\d+)|startpos)(?: moves (?<moves>.*))?");

    Thread uciThread;

    public UciProtocol(UciListener listener) {
        this.listener = listener;
        in = new Scanner(System.in);
    }

    public void start() {
        in = new Scanner(System.in);
        setupAllCommands();
        uciThread = new Thread(this::uciProtocol);

        uciThread.start();
    }

    private void setupAllCommands() {
        commands.put("uci", this::uci);
        commands.put("setoption", this::setOption);
        commands.put("ucinewgame", this::uciNewGame);
        commands.put("isready", this::isReady);
        commands.put("position", this::position);
        commands.put("go", this::go);
        commands.put("print", this::print);
        commands.put("help", this::help);
        commands.put("quit", this::quit);
    }

    private void isReady(String ignore) {
        listener.getReady();
        out.println("readyok");
    }

    private void uciNewGame(String ignore) {
        System.out.println("[TODO]: cleanup; a new game is starting");
    }

    private void quit(String ignore) {
        Model.setRunning(false);
    }

    private void help(String ignore) {
        out.println("\nrhythm is a work in progress UCI chess engine to be used with a GUI.\r\n" +
            "For any further information, visit https://github.com/CarterHidalgo/rhythm\r\n" + 
            "or read the corresponding README.md file distributed along with this program.\r\n");
    }

    private void print(String param) {
        listener.print(param);
    }

    private final Pattern[] goNumeralPatterns = new Pattern[] {
        patternWhiteTime, patternWhiteTimeIncr, patternBlackTime, patternBlackTimeIncr,
        patternMovesToGo, patternDepth, patternNodes, patternMate, patternMovetime, patternPerft
    };

    private void go(String params) {
        List<Optional<Integer>> options = Arrays.stream(goNumeralPatterns).map(pattern -> {
            Matcher matcher = pattern.matcher(params);
            Optional<Integer> option = Optional.empty();
            if(matcher.matches()) {
                option = Optional.of(Integer.parseInt(matcher.group(1)));
            }
            return option;
        }).collect(Collectors.toList());

        Boolean infinite = params.contains("infinite");

        GoParameters goParameters = new GoParameters(options.get(0),
                options.get(1),
                options.get(2),
                options.get(3),
                options.get(4),
                options.get(5),
                options.get(6),
                options.get(7),
                options.get(8),
                options.get(9),
                infinite);

        if(goParameters.getPerft().isPresent()) {
            long start = System.nanoTime();
            int nodes = listener.perft(goParameters.getPerft().get());
            long taken = System.nanoTime() - start;

            out.println("\nnodes: " + nodes);
            out.println("time: " + taken / 1_000_000 + "ms");
            out.println("nps: " + (int) (nodes / (++taken / 1_000_000_000.0)) + "\n");

            return;
        }
        
        new Thread(() -> {
            String bestMove = listener.go(goParameters);
            out.println("bestmove " + bestMove);
        }).start();
    }

    private void position(String position) {
        String[] moves = new String[0];
        String startingPosition = STARTING_POSITION_FEN;

        Matcher matcher = positionPattern.matcher(position);

        if(matcher.matches()) {
            if(matcher.group("fen") != null) {
                startingPosition = matcher.group("fen");
            }

            if(matcher.group("moves") != null) {
                moves = matcher.group("moves").split(" ");
            }
        }

        listener.setPosition(startingPosition, moves);
    }

    private void uci(String ignore) {
        listener.onConnection();
        out.println("id name " + listener.getEngineName());
        out.println("id author " + listener.getAuthorName());
        Iterable<String> options = listener.listSupportedOptions();
        for (String option : options) {
            out.println("option " + option);
        }
        out.println("uciok");
    }

    private void setOption(String option) {
        listener.setOptionValue(option);
    }

    private void uciProtocol() {
        while(Model.isRunning()) {
            String message = in.nextLine();
            String[] commandAndParams = message.split(" ", 2);
            String command = commandAndParams[0];
            String params = commandAndParams.length > 1 ? commandAndParams[1] : "";

            if(commands.containsKey(command)) {
                commands.get(command).accept(params);
            } else {
                out.println("Unknown command \"" + command + "\". Type help for more information.");
            }
        }
    }
}
