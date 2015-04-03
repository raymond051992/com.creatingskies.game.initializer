package com.creatingskies.game.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.main.MainController;

public class LoginController {

	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Login.fxml"));
            AnchorPane login = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	private void login(){
		if(usernameField.getText().isEmpty() ||
				passwordField.getText().isEmpty()){
			new AlertDialog(AlertType.ERROR, "Ooops", "Username and/or password is required.", null).showAndWait();
		}else{
			new MainController().show();
		}
	}
}
