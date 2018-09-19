package Server;
import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Cell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TicTacToeClient extends Application implements TicTacToeConstants {
    //Indicate whether the player has the turn
    private boolean myTurn = false;

    // Indicate the token for the player
    private char myToken = ' ';

    // indicate the token for hte other player
    private char otherToken = ' ';

    // create and initialize cells
    private Cell[][] cell = new Cell[3][3];

    //create and initialize a title label
    private Label lblTitle = new Label();

    //create and initialize a status label
    private Label lblStatus = new Label();

    //indicate selected row and column by the current move
    private int rowSelected;
    private int columnSelected;

    //Input and output streams from/to server
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // continue to play?
    private boolean continueToPlay = true;

    //wait for the player to mark a cell
    private boolean waiting = true;

    //Host name or IP
    private String host = "localhost";

    @Override // Override the start method from the Application class
    public void start(Stage primaryStage){
        // pane to hold cell
        GridPane pane = new GridPane();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                pane.add(cell[i][j] = new Cell(i, j), j, i);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(lblTitle);
        borderPane.setCenter(pane);
        borderPane.setBottom(lblStatus);

        //create a scene and place it in the stage
        Scene scene = new Scene(borderPane, 320, 350);
        primaryStage.setTitle("TicTacToeClient"); // set the stage title
        primaryStage.setScene(scene); //Place scene in the stage
        primaryStage.show(); //Display the stage

        //connect to server
        connectToServer();
    }

    private void connectToServer(){
        try {
            //Create a socket to connect to the server
            Socket socket = new Socket(host, 8000);

            //Create an input stream to receive data to the server
            fromServer = new DataInputStream(socket.getInputStream());

            //Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    //Control the game on a separate thread
    new Thread(() -> {
        try {
            //Get notification from server
            int player = fromServer.readInt();

            //Am I player 1 or 2??
            if (player == PLAYER1){
                myToken = 'X';
                otherToken = 'O';
                Platform.runLater(() -> {
                    lblTitle.setText("Player 1 with token 'X'");
                    lblStatus.setText("Waiting for player 2 to join");
                });

                // Receive startup notification from the server
                fromServer.readInt(); // Whatever read is ignored

                // The other player has joined
                Platform.runLater(() ->
                        lblStatus.setText("Player 2 has joined. I start first"));

                // It is my turn
                myTurn = true;
            }
            else if (player == PLAYER2) {
                myToken = 'O';
                otherToken = 'X';
                Platform.runlater(() -> {
                    lblTitle.setText ("Player 2 with token 'O'");
                    lblStatus.setText ("Waiting for player 1 to move");
                });
            }

            //Continue to play
            while (continueToPlay) {
                if (player == PLAYER1) {
                    waitForPlayerAction(); //wait for player 1 to move
                    sendMove(); //send player 1's move to the server
                    receiveInfoFromServer(); //receive info from the server
                }
                else if (player == PLAYER2) {
                    receiveInfoFromServer(); //receive info from the server
                    waitForPlayerAction(); //wait for player 2 to move
                    sendMove(); //send Player 2's move to the server.
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }).start();


    /** Wait for player to mark a cell **/
    private void waitForPlayerAction () throws interruptedException {
        while (waiting) {
            Thread.sleep(100);
        }

        waiting = true;
    }

    /**Send this player's move to the server **/
    private void sendMove() throws IOException {
        toServer.writeInt(rowSelected); // send the selected row
        toServer.writeInt(columnSelected); //send the selected column
    }

    /** Receive info from the server **/
    private void receiveInfoFromServer() throws IOException {
        //Receive game status
        int status = fromServer.readInt();

        if (status == PLAYER1_WON) {
            //player 1 won, stop playing
            continueToPlay = false;
            if (myToken = 'X') {
                Platform.runLater(() -> lblStatus.setText("I won! (X)"));
            }
            else if (myToken = 'O') {
                Platform.runLater(() ->
                        lblStatus.setText("Player 1 (X) has won!"));
                        receiveMove();
            }
        }
        else if (status == PLAYER2_WON) {
            //Player 2 won, stop playing
            continueToPlay = false;
            if (myToken = 'O') {
                Platform.runLater(() -> lblStatus.setText ("I won! (O)");
            }
            else if (myToken = 'X') {
                Platform.runLater(() ->
                        lblStatus.setText("Player 2 (O) has won!"));
                receiveMove();
            }
        }
        else if (status == DRAW) {
            //No winners, game is over
            continueToPlay = false;
            Platform.runLater(() ->
                    lblStatus.setText("Game is over, no winner!"));

            if (myToken = 'O') {
                receiveMove();
            }
        }
        else{
            receiveMove();
            Platform.runLater(()-> lblStatus.setText("My turn"));
            myTurn = true; //it is my turn
        }
    }

    private void receiveMove() throws IOException {
        //get the other player's move
        int row = fromServer.readInt();
        int column = fromServer.readInt();
        }
        Platform.runLater(() -> cell[row][column].setToken(otherToken));
      }
    }
}
