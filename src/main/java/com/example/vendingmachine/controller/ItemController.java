package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.Item;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

public class ItemController {
    public ImageView ivItemImage;
    public RadioButton btnName;
    public Label lbPrice;

    public void setItem(Item item, ToggleGroup toggleGroup, String id) {
        ivItemImage.setImage(item.getImage());
        btnName.setText(item.getName());
        btnName.setToggleGroup(toggleGroup);
        btnName.setId(id);
        lbPrice.setText(item.getAmount());
    }
}

