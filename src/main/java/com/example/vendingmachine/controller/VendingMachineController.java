package com.example.vendingmachine.controller;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class VendingMachineController implements Initializable {
    public TextArea taOutput;
    public HBox hItems;
    public Button btnNickel;
    public Button btnDime;
    public Button btnQuarter;
    public Button btnDollar;
    public TextArea taInstruction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Node[] nodes = new Node[4];
        for (int i = 0; i < nodes.length; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/item.fxml"));
                ItemController controller = new ItemController();
                loader.setController(controller);
                nodes[i] = loader.load();
                hItems.getChildren().add(nodes[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
