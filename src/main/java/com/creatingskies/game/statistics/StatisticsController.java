package com.creatingskies.game.statistics;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.GameResult;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.util.Util;

public class StatisticsController extends TableViewController {

	@FXML private TableView<GameResult> resultsTable;
	@FXML private TableColumn<GameResult, String> dateColumn;
	@FXML private TableColumn<GameResult, String> gameColumn;
	@FXML private TableColumn<GameResult, String> companyColumn;
	@FXML private TableColumn<GameResult, String> groupColumn;
	@FXML private TableColumn<GameResult, String> speedColumn;
	
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private ChoiceBox<Game> gameChoices;
    @FXML private ChoiceBox<Company> companyChoices;
    
    @FXML private RadioButton dateAscending;
    @FXML private RadioButton dateDescending;
    @FXML private RadioButton speedAscending;
    @FXML private RadioButton speedDescending;
    
    private ToggleGroup dateGroup;
    private ToggleGroup speedGroup;
    
    private GameDao gameDao;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
    
    public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Statistics.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    @FXML
    private void initialize() {
    	super.init();
    	gameDao = new GameDao();
    	gameChoices.getItems().add(null);
		gameChoices.getItems().addAll(FXCollections
				.observableArrayList(new GameDao().findAllGames()));
		gameChoices.setConverter(new StringConverter<Game>() {
			@Override
			public String toString(Game game) {
				return game != null ? game.getTitle() : null;
			}
			
			@Override
			public Game fromString(String string) {
				return null;
			}
		});
		
		companyChoices.getItems().add(null);
		companyChoices.getItems().addAll(FXCollections
				.observableArrayList(new CompanyDAO().findAllCompanies()));
		companyChoices.setConverter(new StringConverter<Company>() {
			@Override
			public String toString(Company company) {
				return company != null ? company.getName() : null;
			}

			@Override
			public Company fromString(String string) {
				return null;
			}
		});
		
		gameChoices.getSelectionModel().selectFirst();
		companyChoices.getSelectionModel().selectFirst();
		
		dateGroup = new ToggleGroup();
		dateAscending.setToggleGroup(dateGroup);
		dateDescending.setToggleGroup(dateGroup);
		dateAscending.setSelected(true);
		
		speedGroup = new ToggleGroup();
		speedAscending.setToggleGroup(speedGroup);
		speedDescending.setToggleGroup(speedGroup);
		speedAscending.setSelected(true);
		
		dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				dateFormat.format(cellData.getValue().getEntryDate())));
		gameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getGame().getTitle()));
		companyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getGroup().getCompany().getName()));
		groupColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getGroup().getName()));
		speedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				computeSpeed(cellData.getValue())));
		
    }
    
    private String computeSpeed(GameResult result){
    	if(result != null){
    		return String.format("%.2f m/s", Util.computeSpeed(result.getDistance(), result.getDuration()));
    	}
    	
    	return null;
    }

    @FXML
    public void filterResults() {
    	Criterion[] criterions = new Criterion[4];
		
		if(startDatePicker.getValue() != null){
			criterions[0] = Restrictions.ge("entryDate",
					Date.from(Instant.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()))));
		}
		
		if(endDatePicker.getValue() != null){
			criterions[1] = Restrictions.le("entryDate",
					Util.addDays(Util.toDate(endDatePicker.getValue()), 1));
		}
		
		if(gameChoices.getValue() != null){
			criterions[2] = Restrictions.eq("game", gameChoices.getValue());
		}
		
		if(companyChoices.getValue() != null){
			criterions[3] = Restrictions.eq("company.name", companyChoices.getValue().getName());
		}
		
		Order dateOrder = dateAscending.isSelected() ? Order.asc("entryDate") : Order.desc("entryDate");
		Order speedOrder = speedAscending.isSelected() ? Order.asc("duration") : Order.desc("duration");
				
    	List<GameResult> results = gameDao.findAllGameResults(dateOrder, speedOrder, criterions);
    	resultsTable.setItems(FXCollections.observableArrayList(results));
    }
    
	@Override
	protected String getViewTitle() {
		return "Statistics";
	}

	@Override
	public TableView<? extends IRecord> getTableView() {
		return resultsTable;
	}

}
