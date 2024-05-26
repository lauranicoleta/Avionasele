import java.util.*;

public class GameBoard {
    private char[][] board;
    private List<String[]> configs;
    private int currentConfig;

    public GameBoard() {
        configs = ConfigLoader.loadConfigs();
        //verificare
        for (int i = 0; i < configs.size(); i++) {
            String[] config = configs.get(i);
            System.out.println("Configurație " + (i + 1) + ":");
            for (String line : config) {
                System.out.println(line);
            }
            System.out.println();
        }
        resetBoard();
    }

    public void resetBoard() {
        if (configs.isEmpty()) {
            throw new IllegalStateException("List of configurations is empty. Cannot reset the board.");
        }

        currentConfig = (int) (Math.random() * configs.size());

        if (currentConfig < 0 || currentConfig >= configs.size()) {
            throw new IndexOutOfBoundsException("Generated random index is out of bounds: " + currentConfig);
        }

        board = new char[10][10];
        String[] config = configs.get(currentConfig);
        for (int i = 0; i < 10; i++) {
            if (config[i].length() != 10) {
                throw new IllegalStateException("Rândul " + (i + 1) + " din configurația curentă nu are exact 10 caractere.");
            }
            board[i] = config[i].toCharArray();
        }

        System.out.println("Configuratia curenta este: " + String.valueOf(currentConfig + 1));
        for (String line : config) {
            System.out.println(line);
        }
    }

    public String shoot(int row, int col) {
        char target = board[row][col];
        if (target == 'A' || target == 'B') {
            board[row][col] = 'D';
            return "DOWN";
        } else if (target == '1' || target == '2' || target == '3') {
            board[row][col] = 'H';  // Mark the hit
            return "HIT";
        } else {
            board[row][col] = 'M';  // Mark the miss
            return "MISS";
        }
    }

//    public boolean allPlanesDown() {
//        for (int i = 0; i < 10; i++) {
//            for (int j = 0; j < 10; j++) {
//                if (board[i][j] == 'A' || board[i][j] == 'B') {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    public boolean allPlanesDown() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 'A' || board[i][j] == 'B') {
                    return false;
                }
            }
        }
        return true;
    }

    public char[][] getBoard() {

        return board;
    }
}
