package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.Input;
import com.example.vendingmachine.model.VendingMachine;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class VendingMachineController implements Initializable {
    public TextArea taOutput;
    public TextArea taInstruction;
    public HBox hItems;
    public Button btnNickel;
    public Button btnDime;
    public Button btnQuarter;
    public Button btnDollar;
    public Button btnClose;
    public RadioButton btnToothpaste;
    public RadioButton btnSoap;
    public RadioButton btnSoda;
    public RadioButton btnCookies;
    private VendingMachine vendingMachine;
    private ActionEvent event;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vendingMachine = new VendingMachine();
    }

    public void handleButtonAction(ActionEvent event) {
        this.event = event;
        if (event.getSource() == btnClose) {
            System.exit(0);
        }
    }

    public void nickelButtonAction(ActionEvent event) {
        String message = vendingMachine.run(Input.Money.NICKEL);
        taOutput.appendText(message + '\n');
    }

    public void dimeButtonAction(ActionEvent event) {
        String message = vendingMachine.run(Input.Money.DIME);
        taOutput.appendText(message + '\n');
    }

    public void quarterButtonAction(ActionEvent event) {
        String message = vendingMachine.run(Input.Money.QUARTER);
        taOutput.appendText(message + '\n');
    }

    public void dollarButtonAction(ActionEvent event) {
        String message = vendingMachine.run(Input.Money.DOLLAR);
        taOutput.appendText(message + '\n');
    }
}
