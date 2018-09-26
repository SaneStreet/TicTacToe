package Server;

import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TicTacToeServer extends Application implements TicTacToeConstants {

    private int sessionNo = 1; //session number

    @Override //Man skal override start metoden i Application klassen
    public void start(Stage primaryStage) {
        TextArea serverLog = new TextArea();

        //Scene til TextArea serverLog
        Scene scene = new Scene(new ScrollPane(serverLog), 450, 200); //scene med textfelt, bredde og højde
        primaryStage.setTitle("TTTServer"); //TicTacToeServer titel
        primaryStage.setScene(scene); //Placerer scene i stage
        primaryStage.show(); //viser stage

        //Threads til at håndtere to spillere i TicTacToe spillet
        new Thread(() -> {
            try {
                //Opretter server
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> serverLog.appendText(new Date() + ": Server started at socket " + serverSocket.getLocalPort() + '\n'));

                //Klar til at oprette sessioner for hver af de to spillere
                while (true) {
                    Platform.runLater(() -> serverLog.appendText(new Date() + ": Wait for players to join session " + sessionNo + '\n'));

                    //Tilslut spiller 1
                    Socket player1 = serverSocket.accept();

                    Platform.runLater(() -> {
                        serverLog.appendText(new Date() + ": Player 1 joined session " + sessionNo + '\n');
                        serverLog.appendText("Player 1's IP address " + player1.getInetAddress().getHostAddress() + '\n');
                    });

                    //Note at spilleren er spiller 1
                    new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                    //Tilslut spiller 2
                    Socket player2 = serverSocket.accept();

                    Platform.runLater(() -> {
                        serverLog.appendText(new Date() + ": Player 2 has joined the session " + sessionNo + '\n');
                        serverLog.appendText("Player 2's IP address " + player2.getInetAddress().getHostAddress() + '\n');
                    });

                    //Note at spilleren er spiller 2
                    new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                    //Vis session og forøg sessions nummer
                    Platform.runLater(() ->
                            serverLog.appendText(new Date() + ": Start a thread for session " + sessionNo++ + '\n'));

                    //Start en ny tråd for denne session af to spillere
                    new Thread(new HandleASession(player1, player2)).start();

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();

    }

    //Definér threadklassen til at håndtere en ny session for to spillere.
    class HandleASession implements Runnable, TicTacToeConstants {
        private Socket player1;
        private Socket player2;

        //Laver og initialiserer celler
        private char[][] cell = new char[3][3];

        //Deklarerer variabler til at modtage og sende data mellem spillere og server
        private DataInputStream fromPlayer1;
        private DataOutputStream toPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer2;

        //Deklarerer variabel der styrer hvornår spillet skal fortsættte eller slutte
        private boolean continueToPlay = true;

        //Constructor der bruges til at starte en tråd/session
        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;

            // Initialize cells
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    cell[i][j] = ' ';
        }


        // commit: 1 --- start --- lasse \\
        //all threads need to implement the run method and this is no diffrent
        //in this method i will write the code that makes the program able to connect two people together
        //i need a couple of methods before i can write the run method
        //first we need a method that can be used to send and register moves to and from the two players
        private void sendMove(DataOutputStream out, int row, int column) throws IOException {
            out.writeInt(row);
            out.writeInt(column);
        }

        //then we need a method to determine if the board is full, its important for the logic to work that i call this after every move is mad
        //the same goes for the isWon method
        private boolean isFull() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (cell[i][j] == ' ')
                        return false;
                }
            }
            return true;
        }

        private boolean isWon(char token) {
            for (int i = 0; i < 3; i++)
                if ((cell[i][0] == token) && (cell[i][1] == token) && (cell[i][2] == token)) {
                    return true;
                }

            for (int j = 0; j < 3; j++)
                if ((cell[0][j] == token) && (cell[1][j] == token) && (cell[2][j] == token)) {
                        return true;
                }
                if ((cell[0][0] == token) && (cell[1][1] == token) && (cell[2][2] == token)) {
                    return true;
                }
                if ((cell[2][0] == token) && (cell[1][1] == token) && (cell[0][2] == token)) {
                    return true;
                }

                return false;
            }

        public void run() {
            try {
                DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(player2.getOutputStream());
                DataInputStream fromPlayer2 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

                //here i will make the first player know that it is their turn to make a move
                toPlayer1.writeUTF("det er player1's tur til at vælge");

                //now i will create the while loop that will keep the two clients in communication with each other for a long time
                while (true) {
                    int row = fromPlayer1.readInt();
                    int column = fromPlayer1.readInt();
                    cell[row][column] = 'X';

                    if (isWon('X')) {
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, column);
                        break;
                    } else if (isFull()) {
                        toPlayer1.writeInt(DRAW);
                        toPlayer2.writeInt(DRAW);
                        sendMove(toPlayer2, row, column);
                        break;
                    } else {
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, row, column);
                    }
                    row = fromPlayer2.readInt();
                    column = fromPlayer2.readInt();
                    cell[row][column] = 'O';

                    if (isWon('O')) {
                        toPlayer1.writeInt(PLAYER2_WON);
                        toPlayer2.writeInt(PLAYER2_WON);
                        sendMove(toPlayer1, row, column);
                    } else {
                        toPlayer1.writeInt(CONTINUE);
                        sendMove(toPlayer1, row, column);
                    }
                }
            } catch (IOException e) {
                System.out.println("der er sket en fejl forbindelsen er brudt");
                e.printStackTrace();
            }
        }
    }
}

