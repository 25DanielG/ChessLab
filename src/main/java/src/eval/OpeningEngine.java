package src.eval;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import src.board.Board;
import src.player.SmartPlayer;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class OpeningEngine {
    private MoveNode root;
    private MoveNode current;
    private SmartPlayer player;

    public OpeningEngine(SmartPlayer p) {
        this.root = new MoveNode("");
        this.current = this.root;
        this.player = p;
        loadOpeningBook("./opening_book.csv");
    }

    private void loadOpeningBook(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> lines = reader.readAll();
            for (String[] line : lines) {
                this.current = this.root; // reset to root
                if (line.length != 3)
                    continue; // invalid line
                String[] moves = line[2].split("\\d+\\.");
                String name = line[1];
                for (int i = 1; i < moves.length; ++i) {
                    moves[i] = moves[i].trim();
                    if (moves[i].length() == 0) continue;
                    current = current.addMove(moves[i], name);
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public boolean search(Board board) {
        List<String> seq = board.sequence;
        MoveNode currentNode = root;

        for (String move : seq) {
            currentNode = currentNode.getNextMove(move);
            if (currentNode == null) {
                return false;
            }
        }

        ArrayList<MoveNode> nextMoves = new ArrayList<MoveNode>(currentNode.nextMoves.values());
        if (nextMoves.isEmpty()) return false;
        int rand = (int) (Math.random() * nextMoves.size()); // random
        player.open = nextMoves.get(rand).toString();
        player.line = nextMoves.get(rand).name;
        return true;
    }

    public boolean testTreeConstruction() {
        // test tree with Polish Gambit, Anderssen's Opening
        // 1.a3 a5 2.b4
        MoveNode current = this.root.getNextMove("a3");
        System.out.println(current);
        current.printNextMoves();
        if (!current.toString().equals("a3")) {
            System.out.println("Failed at a3");
            return false;
        }
        current = current.getNextMove("a5");
        System.out.println(current);
        if (!current.toString().equals("a5")) {
            System.out.println("Failed at a5");
            return false;
        }
        current = current.getNextMove("b4");
        System.out.println(current);
        if (!current.toString().equals("b4")) {
            System.out.println("Failed at b4");
            return false;
        }
        return true;
    }
}

class MoveNode {
    String move;
    String name;
    Map<String, MoveNode> nextMoves;

    public MoveNode(String move) {
        this.move = move;
        this.name = null;
        this.nextMoves = new HashMap<>();
    }

    public MoveNode(String move, String name) {
        this.move = move;
        this.name = name;
        this.nextMoves = new HashMap<>();
    }

    public MoveNode addMove(String seq, String name) {
        String[] moves = seq.split(" ");
        MoveNode current = this;
        for (String move : moves) {
            current.nextMoves.putIfAbsent(move, new MoveNode(move, name));
            current = current.nextMoves.get(move);
        }
        return current;
    }

    public MoveNode getNextMove(String move) {
        return this.nextMoves.get(move);
    }

    public void printNextMoves() {
        System.out.print("[ ");
        for (Map.Entry<String, MoveNode> entry : nextMoves.entrySet()) {
            System.out.print(entry.getKey() + " ");
        }
        System.out.println("]");
    }

    public String toString() {
        return this.move;
    }
}
