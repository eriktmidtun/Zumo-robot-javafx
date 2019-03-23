package zumo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    //fxml elements to control
    @FXML
    private Button btn_connect;
    @FXML
    private Button btn_disconnect;
    @FXML
    private Button btn_refresh;
    @FXML
    private Label errorConnect;
    @FXML
    private ChoiceBox<String> connectionBox;
    @FXML
    private TextArea logView;
    @FXML
    private Label strategyLabel;
    @FXML
    private ChoiceBox<String> strategyBox;
    @FXML
    private Button btn_strategy;

    //to control the placement of the app in the window
    private double x,y;

    //controls formatting of logView
    private boolean lineWritten = false;


    //bluetooth commands
    private static final String con = "#con";
    private static final String cRandom = "#random";
    private static final String cSaD = "#sad";
    private static final String cSpin = "#spin";

    //just an easier way to add new strategies
    private static final String[] stratNames ={"Search & Destroy", "Random", "Spin"};

    //variables for sending and receiving
    private boolean connected = false;
    private String lastMessage;

    //Current open port
    private PLabSerial serialConnection = new PLabSerial();

    /**
     * Adds the line to the log window
     * @param logLine
     *      String to print out
     */
    private void addToLogView(String logLine){
        lineWritten = true;
        Date date = new Date();
        String s = String.format("%s -- %s%n",date, logLine);
        logView.appendText(s);
        System.out.print(s);
    }

    private void emptyLineLogView(){
        if (lineWritten){
            logView.appendText("\n");
        }
    }

    //gets the right command to send based on the choice box element selected
    private String stratToCommand(String strategy) {
        String s;
        switch(strategy) {
            case "Search & Destroy":
                s = cSaD;
                break;
            case "Random":
                s = cRandom;
                break;
            case "Spin":
                s = cSpin;
                break;
            default:
                s = "";
        }
        return s;
    }

    @FXML
    void changeStrategy() {
        if (connected){
            String text = strategyBox.getValue();
            String command = stratToCommand(text);
            emptyLineLogView();
            boolean sent = sendCommand(command);
            if (sent){
                addToLogView("Changed strategy to: " + text);
                strategyLabel.setText("-"+text);
            }
            else {
                addToLogView("Couldn't change strategy, try again");
            }
        }
    }

    //disconnect from currently opened port
    public void disconnect(){
        serialConnection.closePort();
        buttonsConnected(false);
        emptyLineLogView();
        addToLogView("Disconnected from port: " + connectionBox.getValue());
        connected = false;
    }

    //tries to connect to the currently selected port from the connectionBox choiceBox
    public void connect(){
        emptyLineLogView();
        addToLogView("Tries to connected to port: " + connectionBox.getValue());
        int index = connectionBox.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            String portName = connectionBox.getValue();
            serialConnection.openPort(portName);
            connected = sendCommand(con);
            if (connected){
                buttonsConnected(true);
                addToLogView("Connected to port: " + connectionBox.getValue());
                return;
            }
        }
        serialConnection.closePort();
        addToLogView("Couldn't connect to port, closes port");
    }

    /*
    disables the correct buttons
     */
    private void buttonsConnected(boolean connected){
        if (connected) {
            btn_connect.setDisable(true);
            btn_disconnect.setDisable(false);
            btn_refresh.setDisable(true);
            btn_strategy.setDisable(false);
        } else {
            btn_connect.setDisable(false);
            btn_disconnect.setDisable(true);
            btn_refresh.setDisable(false);
            btn_strategy.setDisable(true);
        }
    }

    @FXML
    private void listSerialPorts() {
        String[] portNames = serialConnection.getPortNames();
        if (portNames.length > 0){
            ObservableList<String> items = FXCollections.observableArrayList (portNames);
            connectionBox.setItems(items);
            connectionBox.setValue(items.get(portNames.length-1));
        }
        else {
            errorConnect.setText("There are no available ports");
            addToLogView("Error: Found no available ports");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listSerialPorts();
        serialConnection.addListener(this, "messageReceived");
        ObservableList<String> strats = FXCollections.observableArrayList (stratNames);
        strategyBox.setItems(strats);
        strategyBox.setValue(strats.get(0));
        logView.setEditable(false);
    }


    public void messageReceived(String message, int value) {
        System.out.println( "received " + message);
        lastMessage = message;
    }

    /**
     * @param command
     *      The command we try to send to the Arduino
     * @return boolean
     *      true if the command was sent and received back, false otherwise
     */
    private boolean sendCommand(String command) {
        lastMessage = "";
        try {
            addToLogView("Tries to send command: " + command);
            serialConnection.sendMessage(command);
        }
        catch (NullPointerException e){ // if not connected but still opens port
            addToLogView("Failed, No connection");
            return false;
        }
        //wait maximum 5 seconds for response. response is handled by messageReceived()
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        boolean commandReceived = false;
        while (elapsedTime < 5000 ) {
            //System.out.println("lastmessage: " + lastMessage + " , command: " + command);
            if (lastMessage.equals(command)) {
                commandReceived = true;
                break;
            }
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println(elapsedTime);

            // System.out.println(elapsedSeconds);
        }
        if (commandReceived) {
            addToLogView("The command " + command + " was sent and received after " + elapsedTime +"ms");
            return true;
        }
        else {
            addToLogView("Never received response, try again");
            return false;
        }
    }

    /*
    methods to control the window of the javafx app
     */
    @FXML
    void pressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML
    void dragged(MouseEvent event) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX()-x);
        stage.setY(event.getScreenY()-y);
    }

    @FXML
    void close(MouseEvent event) {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.close();
        System.exit(1);
    }
}
