import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.*;
import java.net.*;


/**
 * Opens a c2w_window that can be used for a two-way network chat.
 * The c2w_window can "listen" for a connection request on a port
 * that is specified by the user.  It can request a connection
 * to another GUIChat c2w_window on a specified computer and port.
 * The c2w_window has an input box where the user can enter
 * messages to be sent over the connection.  A connection
 * can be closed by clicking a button in the c2w_window or by
 * closing the c2w_window.  To test the program, several
 * copies of the program can be run on the same computer.
 */
public class C2W_Chat_App extends Application {
    
    public static void main(String[] c2w_args) {
        launch(c2w_args);
    }
    //--------------------------------------------------------------

    /**
     * Possible states of the thread that handles the network connection.
     */
    private enum ConnectionState { LISTENING, CONNECTING, CONNECTED, CLOSED }

    /**
     * Default port number.  This is the initial content of input boxes in
     * the c2w_window that specify the port number for the connection. 
     */
    private static String c2w_defaultPort = "1501";

    /**
     * Default host name.  This is the initial content of the input box that
     * specifies the name of the computer to which a connection request
     * will be sent.
     */
    private static String c2w_defaultHost = "localhost";


    /**
     * The thread that handles the connection; defined by a nested class.
     */
    private volatile ConnectionHandler connection;

    /**
     * Control buttons that C2W_Chat_appear in the c2w_window.
     */
    private Button c2w_listenButton, c2w_connectButton, c2w_closeButton, 
                   c2w_clearButton, c2w_quitButton, c2w_saveButton, c2w_c2w_sendButton;

    /**
     * Input boxes for connection information (port numbers and host names).
     */
    private TextField c2w_listeningPortInput, c2w_remotePortInput, c2w_remoteHostInput;

    /**
     * Input box for messages that will be sent to the other side of the
     * network connection.
     */
    private TextField c2w_messageInput;

    /**
     * Contains a c2w_transcript of messages sent and received, along with
     * information about the progress and state of the connection.
     */
    private TextArea c2w_transcript;
    
    /**
     * The program's c2w_window.
     */

    private Stage c2w_window;
    
    
    /**
     * Set up the GUI and event handling.
     */
    public void start(Stage stage) {
        c2w_window = stage;
        
        c2w_listenButton = new Button("Listen on port:");
        c2w_listenButton.setOnAction( this::c2w_doAction );
        c2w_connectButton = new Button("Connect to:");
        c2w_connectButton.setOnAction( this::c2w_doAction );
        c2w_closeButton = new Button("Disconnect");
        c2w_closeButton.setOnAction( this::c2w_doAction );
        c2w_closeButton.setDisable(true);
        c2w_clearButton = new Button("Clear c2w_Transcript");
        c2w_clearButton.setOnAction( this::c2w_doAction );
        c2w_c2w_sendButton = new Button("c2w_Send");
        c2w_c2w_sendButton.setOnAction( this::c2w_doAction );
        c2w_c2w_sendButton.setDisable(true);
        c2w_c2w_sendButton.setDefaultButton(true);
        c2w_saveButton = new Button("Save c2w_Transcript");
        c2w_saveButton.setOnAction( this::c2w_doAction );
        c2w_quitButton = new Button("Quit");
        c2w_quitButton.setOnAction( this::c2w_doAction );
        c2w_messageInput = new TextField();
        c2w_messageInput.setOnAction( this::c2w_doAction );
        c2w_messageInput.setEditable(false);
        c2w_transcript = new TextArea();
        c2w_transcript.setPrefRowCount(20);
        c2w_transcript.setPrefColumnCount(60);
        c2w_transcript.setWrapText(true);
        c2w_transcript.setEditable(false);
        c2w_listeningPortInput = new TextField(c2w_defaultPort);
        c2w_listeningPortInput.setPrefColumnCount(5);
        c2w_remotePortInput = new TextField(c2w_defaultPort);
        c2w_remotePortInput.setPrefColumnCount(5);
        c2w_remoteHostInput = new TextField(c2w_defaultHost);
        c2w_remoteHostInput.setPrefColumnCount(18);
        
        HBox c2w_buttonBar = new HBox(5, c2w_quitButton, c2w_saveButton, c2w_clearButton, c2w_closeButton);
        c2w_buttonBar.setAlignment(Pos.CENTER);
        HBox c2w_connectBar = new HBox(5, c2w_listenButton, c2w_listeningPortInput, c2w_connectButton, 
                                      c2w_remoteHostInput, new Label("port:"), c2w_remotePortInput);
        c2w_connectBar.setAlignment(Pos.CENTER);
        VBox topPane = new VBox(8, c2w_connectBar, c2w_buttonBar);
        BorderPane c2w_inputBar = new BorderPane(c2w_messageInput);
        c2w_inputBar.setLeft( new Label("Your Message:"));
        c2w_inputBar.setRight(c2w_c2w_sendButton);
        BorderPane.setMargin(c2w_messageInput, new Insets(0,5,0,5));
        
        BorderPane c2w_root = new BorderPane(c2w_transcript);
        c2w_root.setTop(topPane);
        c2w_root.setBottom(c2w_inputBar);
        c2w_root.setStyle("-fx-border-color: #444; -fx-border-width: 3px");
        c2w_inputBar.setStyle("-fx-padding:5px; -fx-border-color: #444; -fx-border-width: 3px 0 0 0");
        topPane.setStyle("-fx-padding:5px; -fx-border-color: #444; -fx-border-width: 0 0 3px 0");

        Scene scene = new Scene(c2w_root);
        stage.setScene(scene);
        stage.setTitle("Two-user Networked Chat");
        stage.setOnHidden( e -> {
               // If a connection exists when the c2w_window is closed, close the connection.
            if (connection != null) 
                connection.close(); 
        });
        stage.show();

    } // end start()
    
    
    /**
     * A little wrC2W_Chat_apper for showing an error alert.
     */
    private void c2w_errorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    /**
     * Defines responses to buttons.  (In this program, I use one
     * method to handle all the buttons; the source of the event
     * can be used to determine which button was clicked.)
     */
    private void c2w_doAction(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == c2w_listenButton) {
            if (connection == null || 
                    connection.getConnectionState() == ConnectionState.CLOSED) {
                String portString = c2w_listeningPortInput.getText();
                int port;
                try {
                    port = Integer.parseInt(portString);
                    if (port < 0 || port > 65535)
                        throw new NumberFormatException();
                }
                catch (NumberFormatException e) {
                    c2w_errorMessage(portString + "is not a legal port number.");
                    return;
                }
                c2w_connectButton.setDisable(true);
                c2w_listenButton.setDisable(true);
                c2w_closeButton.setDisable(false);
                connection = new ConnectionHandler(port);
            }
        }
        else if (source == c2w_connectButton) {
            if (connection == null || 
                    connection.getConnectionState() == ConnectionState.CLOSED) {
                String portString = c2w_remotePortInput.getText();
                int port;
                try {
                    port = Integer.parseInt(portString);
                    if (port < 0 || port > 65535)
                        throw new NumberFormatException();
                }
                catch (NumberFormatException e) {
                    c2w_errorMessage(portString +"is not a legal port number.");
                    return;
                }
                c2w_connectButton.setDisable(true);
                c2w_listenButton.setDisable(true);
                connection = new ConnectionHandler(c2w_remoteHostInput.getText(),port);
            }
        }
        else if (source == c2w_closeButton) {
            if (connection != null)
                connection.close();
        }
        else if (source == c2w_clearButton) {
            c2w_transcript.setText("");
        }
        else if (source == c2w_quitButton) {
            try {
                c2w_window.hide();
            }
            catch (SecurityException e) {
            }
        }
        else if (source == c2w_saveButton) {
            c2w_doSave();
        }
        else if (source == c2w_c2w_sendButton || source == c2w_messageInput) {
            if (connection != null && 
                    connection.getConnectionState() == ConnectionState.CONNECTED) {
                connection.c2w_send(c2w_messageInput.getText());
                c2w_messageInput.selectAll();
                c2w_messageInput.requestFocus();
            }
        }
    }
    

    /**
     * Save the contents of the c2w_transcript area to a file selected by the user.
     */
    private void c2w_doSave() {
        FileChooser fileDialog = new FileChooser(); 
        fileDialog.setInitialFileName("c2w_transcript.txt");
        fileDialog.setInitialDirectory(new File(System.getProperty("user.home")));
        fileDialog.setTitle("Select File to be Saved");
        File selectedFile = fileDialog.showSaveDialog(c2w_window);
        if (selectedFile == null)
            return;  // User canceled or clicked the dialog's close box.
        PrintWriter out; 
        try {
            FileWriter stream = new FileWriter(selectedFile); 
            out = new PrintWriter( stream );
        }
        catch (Exception e) {
            c2w_errorMessage("Sorry, but an error occurred while\ntrying to open the file:\n" + e);
            return;
        }
        try {
            out.print(c2w_transcript.getText());  // Write text from the TextArea to the file.
            out.close();
            if (out.checkError())   // (need to check for errors in PrintWriter)
                throw new IOException("Error check failed.");
        }
        catch (Exception e) {
            c2w_errorMessage("Sorry, but an error occurred while\ntrying to write the text:\n" + e);
        }    
    }


    /**
     * Add a line of text to the c2w_transcript area.
     * @param message text to be added; a line feed is added at the end
     */
    private void c2w_postMessage(String message) {
        Platform.runLater( () -> c2w_transcript.appendText(message + '\n') );
    }


    /**
     * Defines the thread that handles the connection.  The thread is responsible
     * for opening the connection and for receiving messages.  This class contains
     * several methods that are called by the main class, and that are therefore
     * executed in a different thread.  Note that by using a thread to open the
     * connection, any blocking of the graphical user interface is avoided.  By
     * using a thread for reading messages sent from the other side, the messages
     * can be received and posted to the c2w_transcript asynchronously at the same
     * time as the user is typing and c2w_sending messages.  All changes to the GUI
     * that are made by this class are done using Platform.runLater().
     */
    private class ConnectionHandler extends Thread {

        private volatile ConnectionState state;
        private String remoteHost;
        private int port;
        private ServerSocket listener;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        /**
         * Listen for a connection on a specified port.  The constructor
         * does not perform any network operations; it just sets some
         * instance variables and starts the thread.  Note that the
         * thread will only listen for one connection, and then will
         * close its server socket.
         */
        ConnectionHandler(int port) {
            state = ConnectionState.LISTENING;
            this.port = port;
            c2w_postMessage("\nLISTENING ON PORT " + port + "\n");
            try { setDaemon(true); }
            catch (Exception e) {}
            start();
        }

        /**
         * Open a connection to specified computer and port.  The constructor
         * does not perform any network operations; it just sets some
         * instance variables and starts the thread.
         */
        ConnectionHandler(String remoteHost, int port) {
            state = ConnectionState.CONNECTING;
            this.remoteHost = remoteHost;
            this.port = port;
            c2w_postMessage("\nCONNECTING TO " + remoteHost + " ON PORT " + port + "\n");
            try { setDaemon(true); }
            catch (Exception e) {}
            start();
        }

        /**
         * Returns the current state of the connection.  
         */
        synchronized ConnectionState getConnectionState() {
            return state;
        }

        /**
         * c2w_Send a message to the other side of the connection, and post the
         * message to the c2w_transcript.  This should only be called when the
         * connection state is ConnectionState.CONNECTED; if it is called at
         * other times, it is ignored.  (Although it is unlikely, it is
         * possible for this method to block, if the system's buffer for
         * outgoing data fills.)
         */
        synchronized void c2w_send(String message) {
            if (state == ConnectionState.CONNECTED) {
                c2w_postMessage("c2w_SEND:  " + message);
                out.println(message);
                out.flush();
                if (out.checkError()) {
                    c2w_postMessage("\nERROR OCCURRED WHILE TRYING TO c2w_SEND DATA.");
                    close();
                }
            }
        }

        /**
         * Close the connection. If the server socket is non-null, the
         * server socket is closed, which will cause its accept() method to
         * fail with an error.  If the socket is non-null, then the socket
         * is closed, which will cause its input method to fail with an
         * error.  (However, these errors will not be reported to the user.)
         */
        synchronized void close() {
            state = ConnectionState.CLOSED;
            try {
                if (socket != null)
                    socket.close();
                else if (listener != null)
                    listener.close();
            }
            catch (IOException e) {
            }
        }

        /**
         * This is called by the run() method when a message is received from
         * the other side of the connection.  The message is posted to the
         * c2w_transcript, but only if the connection state is CONNECTED.  (This
         * is because a message might be received after the user has clicked
         * the "Disconnect" button; that message should not be seen by the
         * user.)
         */
        synchronized private void received(String message) {
            if (state == ConnectionState.CONNECTED)
                c2w_postMessage("RECEIVE:  " + message);
        }

        /**
         * This is called by the run() method when the connection has been
         * successfully opened.  It enables the correct buttons, writes a
         * message to the c2w_transcript, and sets the connected state to CONNECTED.
         */
        synchronized private void connectionOpened() throws IOException {
            listener = null;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            state = ConnectionState.CONNECTED;
            Platform.runLater( () -> { 
                c2w_closeButton.setDisable(false);
                c2w_c2w_sendButton.setDisable(false);
                c2w_messageInput.setEditable(true);
                c2w_messageInput.setText("");
                c2w_messageInput.requestFocus();
                c2w_postMessage("CONNECTION ESTABLISHED\n");
            });
        }

        /**
         * This is called by the run() method when the connection is closed
         * from the other side.  (This is detected when an end-of-stream is
         * encountered on the input stream.)  It posts a message to the
         * c2w_transcript and sets the connection state to CLOSED.
         */
        synchronized private void connectionClosedFromOtherSide() {
            if (state == ConnectionState.CONNECTED) {
                c2w_postMessage("\nCONNECTION CLOSED FROM OTHER SIDE\n");
                state = ConnectionState.CLOSED;
            }
        }

        /**
         * Called from the finally clause of the run() method to clean up
         * after the network connection closes for any reason.
         */
        private void cleanUp() {
            state = ConnectionState.CLOSED;
            Platform.runLater( () -> {
                c2w_listenButton.setDisable(false);
                c2w_connectButton.setDisable(false);
                c2w_closeButton.setDisable(true);
                c2w_c2w_sendButton.setDisable(true);
                c2w_messageInput.setEditable(false);
                c2w_postMessage("\n*** CONNECTION CLOSED ***\n");
            });
            if (socket != null && !socket.isClosed()) {
                // Make sure that the socket, if any, is closed.
                try {
                    socket.close();
                }
                catch (IOException e) {
                }
            }
            socket = null;
            in = null;
            out = null;
            listener = null;
        }


        /**
         * The run() method that is executed by the thread.  It opens a
         * connection as a client or as a server (depending on which 
         * constructor was used).
         */
        public void run() {
            try {
                if (state == ConnectionState.LISTENING) {
                        // Open a connection as a server.
                    listener = new ServerSocket(port);
                    socket = listener.accept();
                    listener.close();
                }
                else if (state == ConnectionState.CONNECTING) {
                        // Open a connection as a client.
                    socket = new Socket(remoteHost,port);
                }
                connectionOpened();  // Set up to use the connection.
                while (state == ConnectionState.CONNECTED) {
                        // Read one line of text from the other side of
                        // the connection, and report it to the user.
                    String input = in.readLine();
                    if (input == null)
                        connectionClosedFromOtherSide();
                    else
                        received(input);  // Report message to user.
                }
            }
            catch (Exception e) {
                    // An error occurred.  Report it to the user, but not
                    // if the connection has been closed (since the error
                    // might be the expected error that is generated when
                    // a socket is closed).
                if (state != ConnectionState.CLOSED)
                    c2w_postMessage("\n\n ERROR:  " + e);
            }
            finally {  // Clean up before terminating the thread.
                cleanUp();
            }
        }

    } // end nested class ConnectionHandler

}