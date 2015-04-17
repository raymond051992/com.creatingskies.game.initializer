package com.creatingskies.game.editor;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.user.User;

public class GameController extends TableViewController{
	
	@FXML private TableView<Game> gamesTable;
	@FXML private TableColumn<Game, String> titleColumn;
	@FXML private TableColumn<Game, String> descriptionColumn;
	@FXML private TableColumn<Game, String> typeColumn;
	@FXML private TableColumn<User, Object> actionColumn;
	
	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		
		titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
		descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
		
		actionColumn.setCellFactory(generateCellFactory(Action.VIEW, Action.EDIT, Action.DELETE));
		resetTableView();
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Game.fxml"));
            AnchorPane games = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(games);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	protected void viewRecord(IRecord record) {
		new GamePropertiesController().show(Action.VIEW, (Game) record);
	}
	
	@Override
	protected void editRecord(IRecord record) {
		new GamePropertiesController().show(Action.EDIT, (Game) record);
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
				"Are you sure you want to delete this game?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			super.deleteRecord(record);
			try {
				new GameDao().delete(record);
				resetTableView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void resetTableView(){
		gamesTable.setItems(FXCollections.observableArrayList(new GameDao().findAllGames()));
	}
	
	public void addNewGame(){
		new GamePropertiesController().show(Action.ADD, new Game());
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return gamesTable;
	}

	@Override
	protected String getViewTitle() {
		return "Games";
	}

}
