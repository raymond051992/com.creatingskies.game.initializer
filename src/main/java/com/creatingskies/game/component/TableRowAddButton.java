package com.creatingskies.game.component;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.creatingskies.game.common.MainLayout;

public class TableRowAddButton extends Button{
	
	private static final Image image = 
			new Image(MainLayout.class.getResourceAsStream("/images/plus_ffffff_32.png"),16,16,true,true);
	
	public TableRowAddButton() {
		super("a",new ImageView(image));
		getStyleClass().add("table-row-button");
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}
}
