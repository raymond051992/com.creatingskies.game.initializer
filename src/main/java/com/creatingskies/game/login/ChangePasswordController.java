package com.creatingskies.game.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.model.user.User;
import com.creatingskies.game.model.user.UserDao;
import com.creatingskies.game.util.Util;

public class ChangePasswordController extends ViewController {

	@FXML private TextField usernameField;
	
	@FXML private Label questionLabel;
	@FXML private TextField answerField;
	
	@FXML private TextField passwordField;
	@FXML private TextField confirmPasswordField;
	
	@FXML private VBox usernameBox;
	@FXML private VBox securityQuestionBox;
	@FXML private VBox passwordBox;
	
	private User user;
	
	@Override
	protected String getViewTitle() {
		return "Change Password";
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ChangePassword.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	public void initialize(){
		super.init();
		showBox(usernameBox);
		
		Button backToMainButton = (Button)MainLayout.getPrimaryStage()
				.getScene().lookup("#backToMainButton");
		backToMainButton.setVisible(false);
	}
	
	@FXML
	private void validateUsername(){
		if(Util.isBlank(usernameField.getText())){
			new AlertDialog(AlertType.ERROR, "Ooops", "Username is required.", null).showAndWait();
			return;
		}
		
		UserDao userDao = new UserDao();
		user = userDao.findUser(usernameField.getText());
		
		if(user == null){
			new AlertDialog(AlertType.ERROR, "Ooops", "Invalid username.", null).showAndWait();
			return;
		}
		
		questionLabel.setText(user.getSecurityQuestion().getQuestion());
		showBox(securityQuestionBox);
	}
	
	@FXML
	private void validateSecurityQuestion(){
		if(Util.isBlank(answerField.getText())){
			new AlertDialog(AlertType.ERROR, "Ooops", "Answer is required.", null).showAndWait();
			return;
		}
		
		if(!answerField.getText().equalsIgnoreCase(user.getSecurityQuestionAnswer())){
			new AlertDialog(AlertType.ERROR, "Ooops", "Invalid answer. Please try again.", null).showAndWait();
			return;
		}
		
		showBox(passwordBox);
	}
	
	@FXML
	private void validatePassword(){
		String errorMessage = "";
		
		if(Util.isBlank(passwordField.getText())){
			errorMessage += "Password is required.\n";
		}
		
		if(Util.isBlank(confirmPasswordField.getText())){
			errorMessage += "Password confirmation is required.\n";
		}
		
		if(!errorMessage.isEmpty()){
			new AlertDialog(AlertType.ERROR, "Ooops", errorMessage, null).showAndWait();
			return;
		}
		
		if(!passwordField.getText().equals(confirmPasswordField.getText())){
			new AlertDialog(AlertType.ERROR, "Ooops", "Passwords do not match.", null).showAndWait();
			return;
		}
		
		UserDao userDao = new UserDao();
		user.setPassword(passwordField.getText());
		userDao.saveOrUpdate(user);
		
		new AlertDialog(AlertType.INFORMATION, "Done", "Password has been updated.", null).showAndWait();
		
		showLogin();
	}
	
	@FXML
	private void showLogin(){
		super.close();
		new LoginController().show();
	}
	
	private void showBox(VBox box){
		usernameBox.setVisible(usernameBox.equals(box));
		usernameBox.setManaged(usernameBox.equals(box));
		
		securityQuestionBox.setVisible(securityQuestionBox.equals(box));
		securityQuestionBox.setManaged(securityQuestionBox.equals(box));
		
		passwordBox.setVisible(passwordBox.equals(box));
		passwordBox.setManaged(passwordBox.equals(box));
	}
	
	

}
