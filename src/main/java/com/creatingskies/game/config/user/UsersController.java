package com.creatingskies.game.config.user;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.user.User;
import com.creatingskies.game.model.user.User.Type;
import com.creatingskies.game.model.user.UserDao;
import com.creatingskies.game.model.user.User.Status;

public class UsersController extends TableViewController {

	@FXML private TableView<User> usersTable;
	@FXML private TableColumn<User, String> firstNameColumn;
	@FXML private TableColumn<User, String> lastNameColumn;
	@FXML private TableColumn<User, String> usernameColumn;
	@FXML private TableColumn<User, String> typeColumn;
	@FXML private TableColumn<User, String> statusColumn;
	@FXML private TableColumn<User, Object> actionColumn;
	
	@FXML private ChoiceBox<Type> typeChoices;
	@FXML private ChoiceBox<Status> statusChoices;
	
	@Override
	protected String getViewTitle() {
		return "Users";
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Users.fxml"));
            AnchorPane users = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getFirstName()));
		lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getLastName()));
		usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getUsername()));
		typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getType().toString()));
		statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getStatus().toString()));
		
		
		typeChoices.getItems().add(null);
		typeChoices.getItems().addAll(FXCollections.observableArrayList(Type.values()));
		
		statusChoices.getItems().add(null);
		statusChoices.getItems().addAll(FXCollections.observableArrayList(Status.values()));
		
		typeChoices.getSelectionModel().selectFirst();
		statusChoices.getSelectionModel().selectFirst();
		
		actionColumn.setCellFactory(generateCellFactory(Action.ACTIVATE,
				Action.EDIT, Action.VIEW));
		resetTableView();
	}
	
	private void resetTableView(){
		resetTableView(null, null);
	}
	
	private void resetTableView(Type type, Status status){
		usersTable.setItems(FXCollections.observableArrayList(new UserDao()
			.findFilteredUsers(type, status)));
	}
	
	@FXML
	private void createNew(){
		new UserPropertiesController().show(Action.ADD, new User());
	}

	@Override
	public TableView<? extends IRecord> getTableView() {
		return usersTable;
	}
	
	@Override
	protected void viewRecord(IRecord record) {
		new UserPropertiesController().show(Action.VIEW, (User) record);
	}
	
	@Override
	protected void editRecord(IRecord record) {
		new UserPropertiesController().show(Action.EDIT, (User) record);
	}
	
	@Override
	protected void activateRecord(IRecord record) {
		User user = (User) record;
		String changeStatusAction = user.getStatus().equals(Status.ACTIVE) ? "Deactivate" : "Activate"; 
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, changeStatusAction + " User",
				"Are you sure you want to " + changeStatusAction.toLowerCase() + " this user?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			super.activateRecord(user);
			try {
				user.setStatus(user.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
				new UserDao().saveOrUpdate(user);
				resetTableView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	private void handleFilter(){
		//TODO Filter by all types/status
		resetTableView(typeChoices.getValue(), statusChoices.getValue());
	}
	
}
