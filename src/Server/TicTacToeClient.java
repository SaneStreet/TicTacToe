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
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
