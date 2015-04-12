package com.creatingskies.game.config.event;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.model.event.GameEventDao;

public class GameEventTableViewController extends TableViewController{

	@FXML private DatePicker filterFromDatePicker;
	@FXML private DatePicker filterToDatePicker;
	@FXML private TableView<GameEvent> eventsTable;
	@FXML private TableColumn<GameEvent, String> companyTableColumn;
	@FXML private TableColumn<GameEvent, String> gameTableColumn;
	@FXML private TableColumn<GameEvent, String> dateTableColumn;
	@FXML private TableColumn<GameEvent, Object> actionTableColumn;

	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		
		companyTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getCompany().getName()));
		gameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getGame().getTitle()));
		dateTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getEventDate().toString()));
		actionTableColumn.setCellFactory(generateCellFactory(Action.DELETE,Action.EDIT,Action.VIEW));
		
		filterFromDatePicker.setValue(Util.toLocalDate(new Date()));
		filterToDatePicker.setValue(Util.toLocalDate(new Date()));
		
		filter();
	}
	
	@FXML
	private void filter(){
		Criterion[] criterions = new Criterion[2];
		
		if(filterFromDatePicker.getValue() != null){
			criterions[0] = Restrictions.ge("eventDate",
					Date.from(Instant.from(filterFromDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()))));
		}
		if(filterToDatePicker.getValue() != null){
			criterions[1] = Restrictions.le("eventDate",
					Util.addDays(Util.toDate(filterToDatePicker.getValue()), 1));
		}
		
		eventsTable.setItems(FXCollections.observableArrayList(new GameEventDao().findAll(criterions)));
	}
	
	@FXML
	private void addRecord(){
		close();
		new GameEventPropertiesViewController().show(Action.ADD, new GameEvent());
	}
	
	@Override
	protected void viewRecord(IRecord record) {
		close();
		new GameEventPropertiesViewController().show(Action.VIEW, (GameEvent) record);
	}
	
	@Override
	protected void editRecord(IRecord record) {
		close();
		new GameEventPropertiesViewController().show(Action.EDIT, (GameEvent) record);
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		GameEvent event = (GameEvent) record;
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Delete Event",
				"Are you sure you want to delete this event?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			try {
				new GameEventDao().delete(event);
				filter();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return eventsTable;
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameEvent.fxml"));
            AnchorPane event = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

	@Override
	protected String getViewTitle() {
		return "Events";
	}

}
