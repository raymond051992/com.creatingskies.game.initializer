package com.creatingskies.game.common;

import javafx.scene.control.Alert;

public class AlertDialog extends Alert{

	public AlertDialog(AlertType alertType,String title,String headerText,String content){
		super(alertType);
        initOwner(MainLayout.getPrimaryStage());
        setTitle(title);
        setHeaderText(headerText);
        setContentText(content);
        getDialogPane().getStylesheets()
        	.add(AlertDialog.class.getResource("../resources/css/dialog.css").toString());
	}
}
