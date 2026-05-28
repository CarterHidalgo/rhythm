/*
 * Author: Carter Hidalgo (based on code from sohamkorade: https://github.com/sohamkorade/autoperft)
 * 
 * Purpose: Run automatic perft tests and bisect missing/extra moves
 */

package engine.helper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import engine.model.Model;
import engine.model.Printer;

public class Autoperft {
    public static final int TEST_SIZE = 130;
    private static final Path perftsuitePath = Paths.get("src", "resources", "perftsuite.txt");
    private static List<String> tests = new ArrayList<>();
    private static List<Long> nps = new ArrayList<>();

    public static final boolean test(String mode) {
        return Autoperft.test(0, mode);
    }

    public static final boolean test(int firstTest, String mode) {
        Teacher teacher = new Teacher();

        if(!teacher.isLoaded()) {
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

        boolean singleMode = mode.equals("single");

        System.out.println((singleMode ? "1 test loaded" : tests.size() + " tests loaded"));

        nps.clear();
        long autoperftStart = System.nanoTime();
        for (int i = firstTest; i < tests.size(); i++) {
            if(singleMode) {
                System.out.println("\nTest 1/1 (#" + (i + 1) + ")");
            } else {
                System.out.println("\nTest " + (i + 1) + "/" + tests.size());
            }
            String[] parts = tests.get(i).split(" ;");

            System.out.println("FEN: " + parts[0]);

            for (int depth = 1; depth < parts.length; depth++) {
                Model.setPosition(parts[0]);
                
                long testStart = System.nanoTime();
                long studentNodes = Model.perft(depth, false);
                long testTaken = (System.nanoTime() - testStart) / 1_000_000_000;
                long testNPS = studentNodes / ++testTaken;

                nps.add(testNPS);

                long teacherNodes = Long.valueOf(parts[depth].split(" ")[1]);
                
                System.out.printf("%-10s%15d ", " Depth: " + depth, studentNodes);
                
                if (studentNodes == teacherNodes) {
                    Printer.green("PASS");
                    System.out.println();
                } else {
                    Printer.red("FAIL");
                    System.out.println(", expected " + teacherNodes);

                    boolean longMode = mode.equals("long");
                    Autoperft.bisect(new BisectContext(teacher, depth, parts[0], longMode));
                    teacher.closeProcess();

                    return false;
                }
            }

            if(singleMode) {
                break;
            }
        }

        long autoperftTaken = (System.nanoTime() - autoperftStart) / 1_000_000_000;
        long avgNPS = nps.stream().mapToLong(Long::longValue).sum() / nps.size();
        Printer.green("All tests passed");
        Printer.white(" (" + Printer.formatTimeDuration(autoperftTaken) + "; avg. nps " + avgNPS + ")\n");
        teacher.closeProcess();

        return true;
    }

    public static final boolean test(FEN fen) {
        System.out.println("[TODO]: autoperft with fen " + fen);
        return false;
    }

    public static final void bisect(boolean longMode) {
        Teacher teacher = new Teacher();

        if(!teacher.isLoaded()) {
            return;
        }

        String fenStr = FEN.create().toString();

        Autoperft.bisect(new BisectContext(teacher, 1, fenStr, longMode));
        teacher.closeProcess();
        Model.setPosition(fenStr);
    }

    private static final void bisect(BisectContext ctx) {
        Autoperft.bisect(
            new BisectContext(
                ctx.teacher, 
                ctx.depth, 
                ctx.fenStr, 
                new ArrayList<>(), 
                new ArrayList<>(), 
                ctx.longMode
            ), 
            true
        );

        Printer.board();
    }
    
    private static final void bisect(BisectContext ctx, boolean root) {
        if(root) {
            System.out.println("\nBisecting in " + (ctx.longMode ? "long" : "normal") + " mode...");
        }
        
        Map<String, Long> teacherMap = ctx.teacher.getPerftMap(ctx.depth, ctx.fenStr, ctx.moves);
        Map<String, Long> studentMap = Model.getPerftMap(ctx.depth, ctx.fenStr, ctx.moves);

        Set<String> extra = new HashSet<>(studentMap.keySet());
        extra.removeAll(teacherMap.keySet());

        Set<String> missing = new HashSet<>(teacherMap.keySet());
        missing.removeAll(studentMap.keySet());

        String indent = " " + "  ".repeat(ctx.moves.size());

        for(Map.Entry<String, Long> entry : teacherMap.entrySet()) {
            String key = entry.getKey();
            Long student = studentMap.get(key);
            
            if(student != null) {
                long teacher = entry.getValue();
                long diff = Math.abs(student - teacher);
                
                if(diff != 0) {
                    if(!ctx.longMode) {
                        ctx.branch.add(key);
                    }

                    ctx.moves.add(key);

                    System.out.print(indent + ctx.depth + ". " + key + " (");
                    Printer.red(student);
                    System.out.print(" != ");
                    Printer.green(teacher);
                    System.out.println(")");

                    if(ctx.depth > 1) {
                        ctx.depth -= 1;
                        Autoperft.bisect(ctx, false);
                    }

                    ctx.moves.removeLast();
                    
                    if(!ctx.longMode) {
                        break;
                    }
                }
            }
        }

        Autoperft.printMissing(missing);
        Autoperft.printExtra(extra);

        if(root) {
            StringBuilder branchFenStr = new StringBuilder(
                ctx.fenStr + 
                (ctx.branch.isEmpty() ? "" : " moves " + 
                String.join(" ", ctx.branch))
            );
            System.out.println("\nBranch: " + branchFenStr);
        }
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

    private static class BisectContext {
        final Teacher teacher;
        int depth;
        final String fenStr;
        final List<String> moves, branch;
        final boolean longMode;

        public BisectContext(Teacher teacher, int depth, String fenStr, boolean longMode) {
            this.teacher = teacher;
            this.depth = depth;
            this.fenStr = fenStr;
            this.moves = new ArrayList<>();
            this.branch = new ArrayList<>();
            this.longMode = longMode;
        }

        public BisectContext(Teacher teacher, int depth, String fenStr, List<String> moves, List<String> branch, boolean longMode) {
            this.teacher = teacher;
            this.depth = depth;
            this.fenStr = fenStr;
            this.moves = moves;
            this.branch = branch;
            this.longMode = longMode;
        }
    }
}
