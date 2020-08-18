package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class VendingMachineController implements Initializable {
    public TextArea taOutput;
    public HBox hItems;
    public Button btnNickel;
    public Button btnDime;
    public Button btnQuarter;
    public Button btnDollar;
    public TextArea taInstruction;
    public Button btnClose;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Item> items = new ArrayList<>();
        items.add(new Item("SODA", new Image(getClass().getResourceAsStream("/icons/soda.png"))));
        items.add(new Item("SODA", new Image(getClass().getResourceAsStream("/icons/soda.png"))));
        items.add(new Item("SODA", new Image(getClass().getResourceAsStream("/icons/soda.png"))));
        items.add(new Item("SODA", new Image(getClass().getResourceAsStream("/icons/soda.png"))));

        Node[] nodes = new Node[items.size()];
        ToggleGroup toggleGroup = new ToggleGroup();

        for (int i = 0; i < nodes.length; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/item.fxml"));
                ItemController controller = new ItemController();
                loader.setController(controller);
                nodes[i] = loader.load();
                controller.setItem(items.get(i), toggleGroup);
                hItems.getChildren().add(nodes[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleButtonAction(ActionEvent event) {
        if(event.getSource() == btnClose){
            System.exit(0);
        }
    }
}
