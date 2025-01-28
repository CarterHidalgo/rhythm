/*
 * Name: Coord.java
 * Author: Adapted from Sebastian Lague 
 *  (https://github.com/SebLague/Chess-Coding-Adventure/blob/Chess-V2-UCI/Chess-Coding-Adventure/src/Core/Board/Coord.cs) Thanks! =]
 * 
 * Purpose: Traverse the board directionally and safely without falling off
 */

package helper;

public class Coord {
    public static final Coord[] rookDirections = {new Coord(1, 0), new Coord(0, 1), new Coord(-1, 0), new Coord(0, -1)};
    public static final Coord[] bishopDirections = {new Coord(1, 1), new Coord(-1, 1), new Coord(-1, -1), new Coord(1, -1)};

    private int rank;
    private int file;

    public Coord(int rank, int file) {
        this.rank = rank;
        this.file = file;
    }

    public Coord(int index) {
        rank = index / 8;
        file = index % 8;
    }

    public int getIndex() {
        return 8 * rank + file;
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }

    public boolean isValid() {
        return rank >= 0 && rank < 8 && file >= 0 && file < 8;
    }

    public boolean isNotEdge() {
        return rank > 0 && rank < 7 && file > 0 && file < 7;
    }

    public Coord add(Coord addend) {
        return new Coord(rank + addend.getRank(), file + addend.getFile());
    }

    public Coord mul(int magnitude) {
        return new Coord(rank * magnitude, file * magnitude);
    }

    public String toString() {
        return "rank: " + this.rank + "\nfile: " + this.file;
    }
}
