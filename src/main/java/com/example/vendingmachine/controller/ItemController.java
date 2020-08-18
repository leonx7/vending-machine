package com.example.vendingmachine.controller;

import com.example.vendingmachine.model.Item;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;

public class ItemController {
    public ImageView ivItemImage;
    public RadioButton btnName;

    public void setItem(Item item, ToggleGroup toggleGroup) {
        ivItemImage.setImage(item.getImage());
        btnName.setText(item.getName());
        btnName.setToggleGroup(toggleGroup);
    }
}
