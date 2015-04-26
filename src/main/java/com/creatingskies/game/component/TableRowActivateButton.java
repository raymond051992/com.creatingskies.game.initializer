package com.creatingskies.game.component;

import com.creatingskies.game.common.MainLayout;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TableRowActivateButton extends Button{
	
	private static final Image image = 
			new Image(MainLayout.class.getResourceAsStream("/images/power-off_ffffff_32.png"),16,16,true,true);
	
	public TableRowActivateButton() {
		super("",new ImageView(image));
		getStyleClass().add("table-row-button");
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}
}
