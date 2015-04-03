package com.creatingskies.game.login.view;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	
	@FXML
    private void initialize() {
		System.out.println("test");
    }
	
	@FXML
	private void login(){
		System.out.println(usernameField.getText());
		System.out.println(passwordField.getText());
	}
}
