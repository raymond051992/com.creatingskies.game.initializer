package com.creatingskies.game.config.user;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.user.SecurityQuestion;
import com.creatingskies.game.model.user.User;
import com.creatingskies.game.model.user.User.Status;
import com.creatingskies.game.model.user.User.Type;
import com.creatingskies.game.model.user.UserDao;

public class UserPropertiesController extends PropertiesViewController{

	@FXML TextField firstNameTextField;
	@FXML TextField lastNameTextField;
	@FXML TextField userNameTextField;
	
	@FXML PasswordField passwordField;
	@FXML PasswordField confirmPasswordField;
	@FXML PasswordField securityAnswerField;
	
	@FXML ChoiceBox<Status> statusChoices;
	@FXML ChoiceBox<Type> typeChoices;
	@FXML ChoiceBox<SecurityQuestion> questionChoices;
	
	@FXML Button backToListButton;
	@FXML Button saveButton;
	@FXML Button cancelButton;
	
	private User getUser(){
		return (User) getCurrentRecord();
	}
	
	private void initFields(){
		if(getCurrentAction() == Action.VIEW){
			firstNameTextField.setEditable(false);
			lastNameTextField.setEditable(false);
			userNameTextField.setEditable(false);
			passwordField.setEditable(false);
			confirmPasswordField.setEditable(false);
			securityAnswerField.setEditable(false);
			statusChoices.setDisable(true);
			typeChoices.setDisable(true);
			questionChoices.setDisable(true);
			backToListButton.setVisible(true);
			saveButton.setVisible(false);
			cancelButton.setVisible(false);
		}else{
			firstNameTextField.setEditable(true);
			lastNameTextField.setEditable(true);
			userNameTextField.setEditable(true);
			passwordField.setEditable(true);
			confirmPasswordField.setEditable(true);
			securityAnswerField.setEditable(true);
			statusChoices.setDisable(false);
			typeChoices.setDisable(false);
			questionChoices.setDisable(false);
			backToListButton.setVisible(false);
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
		}
	}
	
	@Override
	public void init() {
		super.init();
		statusChoices.setItems(FXCollections.observableArrayList(Status.values()));
		typeChoices.setItems(FXCollections.observableArrayList(Type.values()));
		questionChoices.setItems(FXCollections.observableArrayList(new UserDao()
				.findAllSecurityQuestions()));
		
		statusChoices.getSelectionModel().selectFirst();
		typeChoices.getSelectionModel().selectFirst();
		questionChoices.getSelectionModel().selectFirst();
		
		typeChoices.setValue(getUser().getType());
        statusChoices.setValue(getUser().getStatus());
		
		questionChoices.setConverter(new StringConverter<SecurityQuestion>() {
			@Override
			public String toString(SecurityQuestion object) {
				return object.getQuestion();
			}
			
			@Override
			public SecurityQuestion fromString(String string) {
				return null;
			}
		});
		initFields();
	}
	
	@Override
	protected String getViewTitle() {
		if(getCurrentAction() == Action.ADD){
			return "Create New User";
		} else if (getCurrentAction() == Action.EDIT) {
			return "Edit User " + getUser().getFullName();
		} else {
			return "User " + getUser().getFullName();
		}
	}
	
	public void show(Action action, User user){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("UserProperties.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            
            UserPropertiesController controller = (UserPropertiesController) loader.getController();
            controller.setUser(action, user);
            controller.init();
            
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setUser(Action action, User user){
		setCurrentAction(action);
		setCurrentRecord(user);
		
		firstNameTextField.setText(user.getFirstName());
		lastNameTextField.setText(user.getLastName());
		userNameTextField.setText(user.getUsername());
		
		passwordField.setText(user.getPassword());
		confirmPasswordField.setText(user.getPassword());
		securityAnswerField.setText(user.getSecurityQuestionAnswer());
		
		statusChoices.setValue(user.getStatus());
		typeChoices.setValue(user.getType());
		questionChoices.setValue(user.getSecurityQuestion());
		
		if(action.equals(Action.EDIT)){
			userNameTextField.setEditable(false);
			passwordField.setEditable(false);
			confirmPasswordField.setEditable(false);
			securityAnswerField.setEditable(false);
		}
	}
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            getUser().setFirstName(firstNameTextField.getText());
            getUser().setLastName(lastNameTextField.getText());
            getUser().setUsername(userNameTextField.getText());
            getUser().setPassword(passwordField.getText());
            getUser().setType(typeChoices.getValue());
            getUser().setStatus(statusChoices.getValue());
            getUser().setSecurityQuestion(questionChoices.getValue());
            getUser().setSecurityQuestionAnswer(securityAnswerField.getText());
            new UserDao().saveOrUpdate(getUser());
            new UsersController().show();
        }
    }
	
    @FXML
    private void handleCancel() {
        new UsersController().show();
    }
    
    @FXML
    private void backToList(){
    	close();
    	new UsersController().show();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";

        if (firstNameTextField.getText() == null || firstNameTextField.getText().length() == 0) {
            errorMessage += "Firstname is required.\n"; 
        }
        
        if (lastNameTextField.getText() == null || lastNameTextField.getText().length() == 0) {
            errorMessage += "Lastname is required.\n"; 
        }
        
        if (userNameTextField.getText() == null || userNameTextField.getText().length() == 0) {
            errorMessage += "Username is required.\n";
        } else {
        	User user = new UserDao().findActiveUser(userNameTextField.getText());
			if (user != null
					&& (getCurrentAction().equals(Action.EDIT) && user
							.getIdNo() != getUser().getIdNo())) {
        		errorMessage += "Username already exists.\n";
        	}
        }
        
        if (passwordField.getText() == null || passwordField.getText().length() == 0) {
            errorMessage += "Password is required.\n";
        } else if (confirmPasswordField.getText() == null || confirmPasswordField.getText().length() == 0) {
            errorMessage += "Password confirmation is required.\n";
        } else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
        	errorMessage += "Passwords doesn't match.\n";
        }
        
        if (securityAnswerField.getText() == null || securityAnswerField.getText().length() == 0) {
            errorMessage += "Answer for security question is required.\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
			new AlertDialog(AlertType.ERROR, "Invalid fields",
					"Please correct invalid fields.", errorMessage)
					.showAndWait();
            return false;
        }
    }

}
