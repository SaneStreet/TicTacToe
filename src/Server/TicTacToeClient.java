package Server;

import javafx.application.Platform;

import java.io.IOException;

public class TicTacToeClient {





















































































    //Control the game on a separate thread
    new thread(() -> {
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
}

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
        Platform.runLater(() -> cell[row][column].setToken(otherToken));
    }
