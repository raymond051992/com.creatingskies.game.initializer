package com.creatingskies.game.editor;

import java.io.IOException;
import java.util.List;
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
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.model.event.GameEventDao;
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
		new GamePropertiesController().show(Action.VIEW, (Game) record,true,false);
	}
	
	@Override
	protected void editRecord(IRecord record) {
		List<GameEvent> events = new GameEventDao().findAllGameEventByGame((Game) record); 
		if(events == null || (events != null && events.isEmpty())){
			new GamePropertiesController().show(Action.EDIT, (Game) record,true,false);
		}else{
			new AlertDialog(AlertType.INFORMATION, "Game", "", "You cannot edit this game. The record shows that we have an event for this game").showAndWait();
		}
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		List<GameEvent> events = new GameEventDao().findAllGameEventByGame((Game) record); 
		
		if(events == null || (events != null && events.isEmpty())){
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
		}else{
			new AlertDialog(AlertType.ERROR, "Error", "", "You cannot delete this game. The record shows that we have an event for this game").showAndWait();
		}
	}
	
	private void resetTableView(){
		gamesTable.setItems(FXCollections.observableArrayList(new GameDao().findAllGames()));
	}
	
	public void addNewGame(){
		new ChooseCreateTypeDialogController().show();
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
