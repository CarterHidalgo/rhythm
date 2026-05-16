/*
 * Author: Adapted from FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI)
 * 
 * Purpose: Starts and manages UCI threads, inputs, and outputs; interacts with the model via
 * UciListener listener
 */

package engine.uci;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Pattern patternBisect = Pattern.compile("^[1-9]\\d*(?: long)?$");
    // private static final Pattern patternTeacher = Pattern.compile("teacher\\s+[\\w\\s\\\\./:-]+\\.exe");
    private static final Pattern patternPosition =
            Pattern.compile("(?:fen (?<fen>.* \\d+ \\d+)|startpos)(?: moves (?<moves>.*))?");

    private static final Pattern[] goNumeralPatterns = new Pattern[] {patternWhiteTime,
            patternWhiteTimeIncr, patternBlackTime, patternBlackTimeIncr, patternMovesToGo,
            patternDepth, patternNodes, patternMate, patternMovetime, patternPerft};

    private static final String name = "rhythm";
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
        commands.put("bisect", UCI::bisect);
        commands.put("help", UCI::help);
        commands.put("stop", UCI::stop);
        commands.put("quit", UCI::quit);
    }

    public static final void start() {
        uciThread = new Thread(UCI::run);
        uciThread.start();
    }

    private static final void run() {
        while (Model.isRunning()) {
            String message = in.nextLine();
            String[] commandAndParams = message.split(" ", 2);
            String command = commandAndParams[0];
            String params = commandAndParams.length > 1 ? commandAndParams[1] : "";

            if (commands.containsKey(command)) {
                commands.get(command).accept(params);
            } else if(!command.isBlank()) {
                out.println("Unknown command \"" + command + "\". Type help for more information.");
            }
        }
    }

    private static final void uci(String ignore) {
        out.println("id name " + name);
        out.println("id author " + author);

        ArrayList<String> options = new ArrayList<>();
        for (String option : options) {
            out.println("option " + option);
        }

        out.println("uciok");
    }

    private static final void setOption(String option) {
        System.out.println("[TODO]: setOptions");
    }

    private static final void uciNewGame(String ignore) {
        System.out.println("[TODO]: cleanup for a new game");
    }

    private static final void isReady(String ignore) {
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

    private static final void position(String params) {
        String[] moves = null;
        String fenStr = FEN.START_FEN;
        Matcher matcher = patternPosition.matcher(params);

        if (matcher.matches()) {
            if (matcher.group("fen") != null) {
                fenStr = matcher.group("fen");
            }

            if (matcher.group("moves") != null) {
                moves = matcher.group("moves").split(" ");
            }

            if(moves != null) {
                Model.setPosition(fenStr, Arrays.stream(moves).collect(Collectors.toCollection(ArrayList::new)));
            } else {
                Model.setPosition(fenStr);
            }
        } else {
            System.out.println("Invalid arguments: position <fen ... | startpos> [moves ...]");
        }
    }

    private static final void go(String params) {
        ArrayList<Optional<Integer>> options = (ArrayList<Optional<Integer>>) Arrays.stream(goNumeralPatterns).map(pattern -> {
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

            Model.perft(goParameters.getPerft().get(), true);

            return;
        } else {
            new Thread(() -> {
                System.out.println("[TODO]: go find best move");
            }).start();
        }
    }

    private static final void print(String param) {
        if (param.isEmpty()) {
            Printer.board();
        } else {
            Printer.bitboard(param);
        }
    }

    private static final void info(String ignore) {
        out.println("\nTurn: " + GameInfo.getTurnString() + "\nHalfmoves: "
                + GameInfo.getHalfmoves() + "\nFullmoves: " + GameInfo.getFullmoves()
                + "\nCastling: " + GameInfo.getCastlingString() + "\n");
    }

    private static final void autoperft(String param) {
        if(param.equals("long") || param.isEmpty()) {
            Autoperft.test(param);
        } else {
            out.println("Invalid arguments: autoperft [long]");
        }
    }

    private static final void bisect(String param) {
        if(param.isEmpty()) {
            Autoperft.bisect(1, false);
        } else {
            Matcher matcher = patternBisect.matcher(param);

            if(matcher.matches()) {
                String[] params = param.split(" ");

                Autoperft.bisect(Integer.valueOf(params[0]), (params.length > 1 ? true : false));
            } else {
                out.println("Invalid arguments: bisect [depth] [long]");
            }
        }
    }

    private static final void help(String ignore) {
        out.println("Valid commands:\n" + 
                    " - uci\n" + 
                    " - setoption\n" +
                    " - isready\n" + 
                    " - position\n" + 
                    " - go\n" +
                    " - print [name]+\n" + 
                    " - info\n" + 
                    " - autoperft [long]\n" + 
                    " - bisect [depth] [long]\n" + 
                    " - stop\n" + 
                    " - quit\n");
    }

    private static final void stop(String ignore) {
        out.println("[TODO]: stop calculating as soon as possible");
    }

    private static final void quit(String ignore) {
        Model.setRunning(false);
    }
}
