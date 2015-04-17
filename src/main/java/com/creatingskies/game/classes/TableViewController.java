package com.creatingskies.game.classes;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import com.creatingskies.game.component.TableRowActivateButton;
import com.creatingskies.game.component.TableRowDeleteButton;
import com.creatingskies.game.component.TableRowEditButton;
import com.creatingskies.game.component.TableRowViewButton;
import com.creatingskies.game.model.IRecord;

public abstract class TableViewController extends ViewController{
	
	private final int BUTTON_BAR_MIN_WIDTH_MULTIPLIER = 30;
	
	private Action currentAction;
	
	protected Action getCurrentAction() {
		return currentAction;
	}
	
	public abstract TableView<? extends IRecord> getTableView();
	
	@SuppressWarnings("rawtypes")
	protected Callback generateCellFactory(Action... actions){
		return generateCellFactory(getTableView(), actions);
	}
	
	@SuppressWarnings("rawtypes")
	protected Callback generateCellFactory(TableView<? extends IRecord> tableView, Action... actions){
		Callback<TableColumn<? extends IRecord, Object>, TableCell<? extends IRecord, Object>> 
		actionColumnCellFactory = 
            new Callback<TableColumn<? extends IRecord, Object>, TableCell<? extends IRecord, Object>>() {

	        @Override
	        public TableCell<? extends IRecord, Object> call(final TableColumn<? extends IRecord,Object> param) {
				final TableCell<? extends IRecord, Object> cell = new TableCell<IRecord, Object>() {
					@Override
					public void updateItem(Object item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
							setGraphic(null);
						} else {
							setAlignment(Pos.CENTER);
							setGraphic(createButtonBar(tableView, param, getIndex(), actions));
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}
		};
		
		return actionColumnCellFactory;
	}
	
	private HBox createButtonBar(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param,
			Integer index, Action... actions){
		HBox box = new HBox(5);
		box.setAlignment(Pos.CENTER);
		
		for(Action action : actions){
			if(action.equals(Action.VIEW)){
				box.getChildren().add(createViewButton(tableView, param, index));
			} else if(action.equals(Action.EDIT)){
				box.getChildren().add(createEditButton(tableView, param, index));
			} else if(action.equals(Action.DELETE)){
				box.getChildren().add(createDeleteButton(tableView, param, index));
			} else if(action.equals(Action.ACTIVATE)){
				box.getChildren().add(createActivateButton(tableView, param, index));
			}
		}
		
		box.setMinWidth(BUTTON_BAR_MIN_WIDTH_MULTIPLIER * box.getChildren().size());
		return box;
	}
	
	private Button createViewButton(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index){
		Button viewButton = new TableRowViewButton();
		viewButton.setOnAction(createViewEventHandler(tableView, param, index));
		return viewButton;
	}
	
	private Button createEditButton(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index){
		Button editButton = new TableRowEditButton();
		editButton.setOnAction(createEditEventHandler(tableView, param, index));
		return editButton;
	}
	
	private Button createDeleteButton(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index){
		Button deleteButton = new TableRowDeleteButton();
		deleteButton.setOnAction(createDeleteEventHandler(tableView, param, index));
		return deleteButton;
	}
	
	private Button createActivateButton(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index){
		Button activateButton = new TableRowActivateButton();
		activateButton.setOnAction(createActivateEventHandler(tableView, param, index));
		return activateButton;
	}
	
	private EventHandler<ActionEvent> createViewEventHandler(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				param.getTableView().getSelectionModel().select(index);
				IRecord record = tableView.getSelectionModel().getSelectedItem();
				if (record != null) {
					currentAction = Action.VIEW;
					viewRecord(record);
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> createEditEventHandler(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				param.getTableView().getSelectionModel().select(index);
				IRecord record = tableView.getSelectionModel().getSelectedItem();
				if (record != null) {
					currentAction = Action.EDIT;
					editRecord(record);
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> createDeleteEventHandler(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				param.getTableView().getSelectionModel().select(index);
				IRecord record = tableView.getSelectionModel().getSelectedItem();
				if (record != null) {
					currentAction = Action.DELETE;
					deleteRecord(record);
				}
			}
		};
	}
	
	private EventHandler<ActionEvent> createActivateEventHandler(TableView<? extends IRecord> tableView,
			TableColumn<? extends IRecord, Object> param, Integer index) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				param.getTableView().getSelectionModel().select(index);
				IRecord record = tableView.getSelectionModel().getSelectedItem();
				if (record != null) {
					currentAction = Action.ACTIVATE;
					activateRecord(record);
				}
			}
		};
	}
	
	/*
	 * Override this methods to add functionality to action buttons
	 */
	protected void viewRecord(IRecord record){}
	
	protected void editRecord(IRecord record){}
	
	protected void deleteRecord(IRecord record){}
	
	protected void activateRecord(IRecord record){}
	
}
