package com.creatingskies.game.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.UserManager;
import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.main.MainController;
import com.creatingskies.game.model.user.User;
import com.creatingskies.game.model.user.UserDao;

public class LoginController extends ViewController{

	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	
	@Override
	protected String getViewTitle() {
		return null;
	}
	
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
			UserDao userDao = new UserDao();
			User user = userDao.findActiveUser(usernameField.getText());
			
			if(user == null){
				new AlertDialog(AlertType.ERROR, "Ooops", "Inavlid username/password.", null).showAndWait();
				return;
			}
			
			if(!user.getPassword().equals(passwordField.getText())){
				new AlertDialog(AlertType.ERROR, "Ooops", "Inavlid username/password.", null).showAndWait();
				return;
			}
			
			UserManager.setCurrentUser(user);
			new MainController().show();
		}
	}
}
