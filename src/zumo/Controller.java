package zumo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    private Button btn_connect;
    @FXML
    private Button btn_disconnect;
    @FXML
    private Button btn_refresh;
    @FXML
    private Label errorConnect;
    @FXML
    private ChoiceBox connectionBox;
    @FXML
    private ListView<String> logView;
    @FXML
    private Label strategyLabel;
    @FXML
    private ChoiceBox strategyBox;
    @FXML
    private Button btn_strategy;


    //bluetooth commands
    public static final String con = "&con";
    public static final String cRandom = "#random";
    public static final String cSaD = "#sad";
    public static final String cSpin = "#spin";

    public static final String[] stratNames ={"Search & Destroy", "Random", "Spin"};



    private boolean connected = false;
    private boolean recievedMessage = false;
    private String lastMessage;

    private PLabSerial serialConnection = new PLabSerial();


    private void addToLogView(String logLine){
        Date date = new Date();
        logView.getItems().add(String.format("%s -- %s",date,logLine));
    }

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
            String text = strategyBox.getValue().toString();
            String command = stratToCommand(text);
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


    public void disconnect(Event event){
        System.out.println(((Button)event.getSource()).getText());
        serialConnection.closePort();
        buttonsConnected(false);
        addToLogView("Disconnected from port: " + connectionBox.getValue());
        connected = false;
    }

    public void connect(Event event){
        addToLogView("Tries to connected to port: " + connectionBox.getValue());
        System.out.println(((Button)event.getSource()).getText());
        int index = connectionBox.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            String portName = connectionBox.getValue().toString();
            serialConnection.openPort(portName);
            connected = sendCommand(con);
            if (connected){
                buttonsConnected(true);
                addToLogView("Connected to port: " + connectionBox.getValue());
            }
            else {
                serialConnection.closePort();
                addToLogView("Couldn't connect to with port, closes port");
            }
        } else {
            serialConnection.closePort();
            addToLogView("Couldn't connect to port, closes port");
        }
    }

    /*
    disables the correct button
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

    public void refreshPorts(Event event){
        listSerialPorts();
    }

    @FXML
    private void listSerialPorts() {
        String[] portNames = serialConnection.getPortNames();
        if (portNames.length > 0){
            ObservableList<String> items = FXCollections.observableArrayList (portNames);
            connectionBox.setItems(items);
            connectionBox.setValue(items.get(0));
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

    }

    public void messageReceived(String message, int value) {
        System.out.println( "received " + message);
        lastMessage = message;
    }

    private boolean sendCommand(String command) {
        lastMessage = "";
        serialConnection.sendMessage(command);
        addToLogView("Tries to send command: " + command);

        long startTime = System.currentTimeMillis();
        long elapsedSeconds = 0;
        boolean commandReceived = false;
        while (elapsedSeconds < 5 ) {
            System.out.println("lastmessage: " + lastMessage + " , command: " + command);
            if (lastMessage.equals(command)) {
                commandReceived = true;
                break;
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            elapsedSeconds = elapsedTime / 1000;
            // System.out.println(elapsedSeconds);
        }
        if (commandReceived) {
            addToLogView(command + " was sent and received");
            return true;
        }
        else {
            addToLogView("Never received response, try again");
            return false;
        }
    }
}
