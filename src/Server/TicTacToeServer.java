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

public class TicTacToeServer extends Application implements TicTacToeConstants{

    private int sessionNo = 1; //sessions nummer

    @Override //Man skal override start metoden i Application klassen
    public void start(Stage primaryStage){
        TextArea serverLog = new TextArea();

        //Scene til TextArea serverLog
        Scene scene = new Scene(new ScrollPane(serverLog), 450, 200); //scene med textfelt, bredde og højde
        primaryStage.setTitle("TTTServer"); //TicTacToeServer titel
        primaryStage.setScene(scene); //Placerer scene i stage
        primaryStage.show(); //viser stage

        //Threads til at håndtere to spillere i TicTacToe spillet
        new Thread(() -> {
            try{
                //Opretter server
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() -> serverLog.appendText(new Date() + ": Server started at socket " + serverSocket.getLocalPort() + '\n'));

                //Klar til at oprette sessioner for hver af de to spillere
                while(true){
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

                    Platform.runLater(()->{
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
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }).start();

    }
    //Definér threadklassen til at håndtere en ny session for to spillere.
    class HandleASession implements Runnable, TicTacToeConstants {
        private Socket player1;
        private Socket player2;

        //Laver og initialiserer celler
        private char[][] cell =  new char[3][3];

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



        public void run() {
            try {


                DataInputStream toPlayer1 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer1 = new DataOutputStream(player2.getOutputStream());
                DataInputStream toPlayer2 = new DataInputStream(player1.getInputStream());
                DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

            }   catch (IOException e){
                System.out.println("der er sket en fejl forbindelsen er brudt");
            }

        }
    }



































// commit: 1 --- start --- lasse \\
//all threads need to implement the run method and this is no diffrent
//in this method i will write the code that makes the program able to connect two people together
public void run(){
    try {
        DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
        DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
        DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
        DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

        //just for comunication to the player that has to start
        toPlayer1.writeInt(1);

        //now i will make the

        }   catch (IOException e){
        System.out.println("en fejl er opstået forbindelsen er afbrudt");
        }
    }
}

