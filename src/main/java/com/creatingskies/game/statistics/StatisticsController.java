package com.creatingskies.game.statistics;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.GameResult;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.util.Util;

public class StatisticsController extends PropertiesViewController {

	@FXML private BarChart<String, Double> barChart;
    @FXML private CategoryAxis xAxis;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private ChoiceBox<Game> gameChoices;
    @FXML private ChoiceBox<Company> companyChoices;
    
    private GameDao gameDao;
    
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
		gameChoices.getItems()
				.addAll(FXCollections.observableArrayList(new GameDao()
						.findAllGames()));
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
		companyChoices.getItems().addAll(
				FXCollections.observableArrayList(new CompanyDAO()
						.findAllCompanies()));
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
    }

    @FXML
    public void filterResults() {
    	barChart.getData().clear();
    	
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
    	
    	List<GameResult> results = gameDao.findAllGameResults(criterions);
    	
    	XYChart.Series<String, Double> series = null;
        GameResult previousResult = null;
        boolean newGroup = false;
        
        for (GameResult result : results) {
        	if(previousResult == null || !previousResult.equals(result)){
        		series = new XYChart.Series<>();
        		newGroup = true;
        	} else {
        		newGroup = false;
        	}
        	
			series.getData().add(
					new XYChart.Data<>(result.getGroup().getName(), result
							.getDuration()));
			
			if(newGroup){
				barChart.getData().add(series);
			}
        }
    }
    
	@Override
	protected String getViewTitle() {
		return "Statistics";
	}

}
