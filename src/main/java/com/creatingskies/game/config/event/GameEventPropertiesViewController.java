package com.creatingskies.game.config.event;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameConverter;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.model.event.GameEventDao;
import com.creatingskies.game.util.Util;

public class GameEventPropertiesViewController extends PropertiesViewController{

	@FXML private ComboBox<Company> companyComboBox;
	@FXML private ComboBox<Game> gameComboBox;
	@FXML private DatePicker eventDatePicker;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	@FXML private Button backToListButton;
	@FXML private Button playButton;
	
	@SuppressWarnings("unchecked")
	public void init(){
		super.init();
		
		companyComboBox.getItems().clear();
		companyComboBox.getItems().add(null);
		companyComboBox.getItems().addAll((List<Company>) new CompanyDAO().findAll(Company.class));
		companyComboBox.setConverter(new StringConverter<Company>() {
			@Override
			public String toString(Company company) {
				return company.getName();
			}
			
			@Override
			public Company fromString(String string) {
				return null;
			}
		});
		
		gameComboBox.getItems().clear();
		gameComboBox.getItems().add(null);
		gameComboBox.getItems().addAll(new GameDao().findAllGames());
		gameComboBox.setConverter(new GameConverter());
		gameComboBox.setCellFactory(new Callback<ListView<Game>,ListCell<Game>>(){
            @Override
            public ListCell<Game> call(ListView<Game> p) {
                 
                final ListCell<Game> cell = new ListCell<Game>(){
 
                    @Override
                    protected void updateItem(Game t, boolean bln) {
                        super.updateItem(t, bln);
                         
                        if(t != null){
                            setText(t.getTitle());
                        }else{
                            setText(null);
                        }
                    }
  
                };
                return cell;
            }
        });
		
		companyComboBox.getSelectionModel().select(getGameEvent().getCompany());
		gameComboBox.getSelectionModel().select(getGameEvent().getGame());
		
		eventDatePicker.setValue(Util.toLocalDate(getGameEvent().getEventDate()));
		
		disableFields();
	}
	
	private void disableFields(){
		boolean isViewAction = getCurrentAction() == Action.VIEW;
		
		companyComboBox.setDisable(isViewAction);
		gameComboBox.setDisable(isViewAction);
		eventDatePicker.setDisable(isViewAction);
		
		saveButton.setVisible(!isViewAction);
		cancelButton.setVisible(!isViewAction);
		backToListButton.setVisible(isViewAction);
		playButton.setVisible(isViewAction);
	}
	
	@FXML
	private void save(){
		if(isValid()){
			getGameEvent().setCompany(companyComboBox.getValue());
			getGameEvent().setGame(gameComboBox.getValue());
			getGameEvent().setEventDate(getEventDate());
			new GameEventDao().saveOrUpdate(getGameEvent());
			close();
			new GameEventTableViewController().show();
		}
	}
	
	@FXML
	private void cancel(){
		backToList();
	}
	
	@FXML
	private void backToList(){
		close();
		new GameEventTableViewController().show();
	}
	
	@FXML
	private void play(){
		new GameEventGroupSelectionController().show(getGameEvent());
	}
	
	private boolean isValid(){
		if(companyComboBox.getSelectionModel().getSelectedItem() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Group is required.").showAndWait();
			return false;
		}
		if(gameComboBox.getSelectionModel().getSelectedItem() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Game is required.").showAndWait();
			return false;
		}
		if(eventDatePicker.getValue() == null){
			new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Date is required.").showAndWait();
			return false;
		}
		GameEvent event = new GameEventDao().findEventByDate(getEventDate());
		if(event != null){
			new AlertDialog(AlertType.ERROR, "Ooops", null, "Event time is not available.").showAndWait();
			return false;
		}
		
		return true;
	}
	
	private Date getEventDate(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(Date.from(Instant.from(eventDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()))));
		
		return cal.getTime();
	}
	
	public void show(Action action,GameEvent gameEvent){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameEventProperties.fxml"));
            AnchorPane event = (AnchorPane) loader.load();
            
            GameEventPropertiesViewController controller = (GameEventPropertiesViewController) loader.getController();
            controller.setCurrentAction(action);
            controller.setCurrentRecord(gameEvent);
            controller.init();
            
            MainLayout.getRootLayout().setCenter(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	protected String getViewTitle() {
		return "Event Details";
	}
	
	public GameEvent getGameEvent(){
		return (GameEvent) getCurrentRecord();
	}
	
	public void setGameEvent(GameEvent gameEvent){
		setCurrentRecord(gameEvent);
	}
	
}
