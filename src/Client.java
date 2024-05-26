import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static char[][] board = new char[10][10];
    private static boolean isMyTurn = false;
    private static volatile boolean isServerActive = true;

    public static void main(String[] args) {
        initializeBoard();
        printBoard();
        try {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 Scanner scanner = new Scanner(System.in)) {

                System.out.println(in.readLine());  // Read prompt for name
                String name = scanner.nextLine();
                out.println(name);  // Send client name

                Thread clientThread = new Thread(() -> {
                    try {
                        while (isServerActive) {
                            String serverMessage = in.readLine();
                            if (serverMessage != null) {
                                handleServerMessage(serverMessage, name, out, scanner, in);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                clientThread.start();

                while (isServerActive) {
                    if (isMyTurn) {
                        System.out.print("Enter coordinates to shoot (row col): ");
                        String command = scanner.nextLine();
                        out.println("SHOT " + command);
                        isMyTurn = false;  // Reset turn flag
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServerMessage(String serverMessage, String name, PrintWriter out, Scanner scanner, BufferedReader in) {
        if (serverMessage.startsWith("CURRENT BOARD:")) {
            StringBuilder boardMessage = new StringBuilder(serverMessage);
            try {
                for (int i = 0; i < 10; i++) {
                    boardMessage.append("\n").append(in.readLine());
                }
                readBoard(boardMessage.toString());
                printBoard();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(serverMessage);
            if (serverMessage.startsWith("It's now ")) {
                isMyTurn = serverMessage.contains(name);
            } else if (serverMessage.startsWith("SHOT RESULT")) {
                updateBoard(serverMessage);
                printBoard();
            } else if (serverMessage.startsWith("New game started. Current board:")) {
                initializeBoard();
                printBoard();
            }
        }
    }

    private static void readBoard(String serverMessage) {
        String[] lines = serverMessage.split("\n");
        for (int i = 1; i <= 10; i++) { // Începem de la 1 pentru a ignora titlul "CURRENT BOARD:"
            //System.out.println("Line " + i + ": " + lines[i]); // Debug
            if (lines[i].length() != 10) {
                System.out.println("Error: Line " + i + " does not have exactly 10 characters. Found " + lines[i].length() + " characters.");
                return;
            }
            board[i - 1] = lines[i].toCharArray(); // Ajustăm indexul pentru a popula corect board-ul
        }
    }

    private static void initializeBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                board[i][j] = '.';
            }
        }
    }

    private static void updateBoard(String serverMessage) {
        String[] parts = serverMessage.split(" ");
        int row = Integer.parseInt(parts[2]);
        int col = Integer.parseInt(parts[3]);
        String result = parts[4];

        if ("HIT".equals(result)) {
            board[row][col] = '1';
        } else if ("MISS".equals(result)) {
            board[row][col] = 'O';
        } else if ("DOWN".equals(result)) {
            board[row][col] = 'X';
        }
    }

    public static void printBoard() {
        System.out.println("Current board state:");
        System.out.print("  ");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
        }
        System.out.println(" ");
        for (int i = 0; i < 10; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < 10; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}
