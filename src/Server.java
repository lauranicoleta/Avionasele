import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static GameBoard gameBoard = new GameBoard();
    private static List<ClientHandler> clientsList = new ArrayList<>();
    private static int currentPlayerIndex = 0;
    private static char[][] currentBoardState;

    public static void main(String[] args) {
        System.out.println("Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                clientsList.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void broadcast(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }

    public static synchronized void handleShot(String clientName, int row, int col, ClientHandler currentPlayer) {
        if (clientsList.get(currentPlayerIndex) == currentPlayer) {
            String result = gameBoard.shoot(row, col);
            String message = "SHOT RESULT " + row + " " + col + " " + result;
            broadcast(clientName + " shot at (" + row + ", " + col + ") and result is: " + result);
            broadcast(message);  // Send the result to all clients

            if (gameBoard.allPlanesDown()) {
                broadcast("All planes have been shot down by " + clientName + "!");
                gameBoard.resetBoard();  // Reset the game board for a new game
                broadcast("New game started. Current board:");
                //broadcastBoard(); //Daca apelez aceasta functie clientul va primi un mesaj cu congiguratia curenta a boardului
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % clientsList.size();
            broadcast("It's now " + clientsList.get(currentPlayerIndex).getClientName() + "'s turn to shoot.");
        } else {
            currentPlayer.sendMessage("It's not your turn. Wait for your turn.");
        }
    }

    public static synchronized void broadcastBoard() {
        char[][] board = gameBoard.getBoard();
        for (ClientHandler handler : clientHandlers) {
            handler.sendBoard(board);
        }
    }

    //aceasta clasa implementeaza Runnable deoarece atunci cand un client nou
    // se conecteaza sa se creeze un nou thread si sa fie rulate in thread uri separate
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Enter your name: ");
                clientName = in.readLine();
                broadcast(clientName + " has joined the game!");

                if (clientsList.size() == 1) {
                    broadcast("It's now " + clientName + "'s turn to shoot.");
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("SHOT")) {
                        String[] parts = inputLine.split(" ");
                        if (parts.length != 3) {
                            out.println("Invalid format! Please enter coordinates in the format 'SHOT row col'.");
                            continue; // Skip processing invalid input
                        }
                        try {
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            handleShot(clientName, row, col, this);
                        } catch (NumberFormatException e) {
                            out.println("Invalid format! Please enter valid integer coordinates.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientHandlers.remove(this);
                clientsList.remove(this);
            }
        }

        public String getClientName() {
            return clientName;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void sendBoard(char[][] board) {
            out.println("CURRENT BOARD:");
            for (int i = 0; i < 10; i++) {
                String line = new String(board[i]);
                out.println(line);
            }
        }
    }
}
