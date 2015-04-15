package com.creatingskies.game.config.weather;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.weather.Weather;
import com.creatingskies.game.model.weather.WeatherDAO;

public class WeathersController extends TableViewController {

	@FXML TableView<Weather> weathersTable;
	@FXML TableColumn<Weather, String> nameColumn;
	@FXML TableColumn<Weather, String> imageColumn;
	@FXML TableColumn<Weather, String> gameTypeColumn;
	@FXML TableColumn<Weather, String> audioColumn;
	@FXML TableColumn<Weather, String> difficultyColumn;
	@FXML TableColumn<Weather, Object> actionColumn;
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Weathers.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@SuppressWarnings("unchecked")
	@FXML
	public void initialize(){
		super.init();
		
		nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getName()));
		imageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getImageFileName()));
		gameTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				getGameTypeDisplay(cellData.getValue())));
		
		audioColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getAudioFileName()));
		
		difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getDifficulty() != null ? cellData
						.getValue().getDifficulty().toString() : null));
		
		actionColumn.setCellFactory(generateCellFactory(Action.DELETE, Action.EDIT));
		resetTableView();
	}
	
	private void resetTableView(){
		WeatherDAO weatherDAO = new WeatherDAO();
		weathersTable.setItems(FXCollections.observableArrayList(weatherDAO
				.findAll()));
	}
	
	private String getGameTypeDisplay(Weather weather){
		String displayString = "";
		
		if(weather.getForRowing()){
			displayString += Type.ROWING.toString() + ". ";
		}
		
		if(weather.getForCycling()){
			displayString += Type.CYCLING.toString() + ". ";
		}
		
		return displayString;
	}
	
	@FXML
	private void handleAdd() {
		new WeatherPropertiesController().show(Action.ADD, new Weather());
	}

	@Override
	protected void editRecord(IRecord record) {
		super.editRecord(record);
		
		if(record instanceof Weather){
			new WeatherPropertiesController().show(Action.ADD, (Weather) record);
		}
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
				"Are you sure you want to delete this weather?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			super.deleteRecord(record);
			try {
				new WeatherDAO().delete(record);
				resetTableView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return weathersTable;
	}
	
	@Override
	protected String getViewTitle() {
		return "Weather";
	}

}
