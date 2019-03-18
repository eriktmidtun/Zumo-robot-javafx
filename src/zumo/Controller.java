package zumo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    private Button btn_connect; // button at bottom right, for starting pvp Game
    @FXML
    private Button btn_disconnect;
    @FXML
    private Button btn_refresh;
    @FXML
    private Label errorConnect;
    @FXML
    private ChoiceBox connectionBox;

    private PLabSerial serialConnection = new PLabSerial();


    public void disconnect(Event event){

        System.out.println(((Button)event.getSource()).getText());
        errorConnect.setText(" ");
        serialConnection.closePort();
        buttonsConnected(false);

    }
    public void connect(Event event){
        System.out.println(((Button)event.getSource()).getText());
        errorConnect.setText(" ");
        int index = connectionBox.getSelectionModel().getSelectedIndex();
        if (index > -1) {
            String portName = connectionBox.getValue().toString();
            serialConnection.openPort(portName);
            buttonsConnected(true);
        } else {
            errorConnect.setText("Couldn't connect to port");
        }
    }


    /*
    disables the currect button
     */
    private void buttonsConnected(boolean connected){
        if (connected) {
            btn_connect.setDisable(true);
            btn_disconnect.setDisable(false);
            btn_refresh.setDisable(true);
        } else {
            btn_connect.setDisable(false);
            btn_disconnect.setDisable(true);
            btn_refresh.setDisable(false);
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
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listSerialPorts();
    }
}
