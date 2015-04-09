package com.creatingskies.game.statistics;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;

public class StatisticsController extends PropertiesViewController{

	@FXML private BarChart<String, Integer> barChart;
    @FXML private CategoryAxis xAxis;

    private ObservableList<String> monthNames = FXCollections.observableArrayList();
    private boolean setAlready;
    
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
    	String[] months = DateFormatSymbols.getInstance(Locale.ENGLISH).getMonths();
        monthNames.addAll(Arrays.asList(months));
        xAxis.setCategories(monthNames);
    }

    @FXML
    public void generate() {
    	if(!setAlready){
    		setDummyData();
        	setDummyData();
        	setDummyData();
        	setAlready = true;
    	}
    }
    
    public void setDummyData(){
    	int[] monthCounter = new int[12];
        
        for (int i = 0; i < 100; i++) {
        	Random random = new Random();
            int month = random.nextInt(12);
            monthCounter[month]++;
        }

        XYChart.Series<String, Integer> series = new XYChart.Series<>();

        for (int i = 0; i < monthCounter.length; i++) {
            series.getData().add(new XYChart.Data<>(monthNames.get(i), monthCounter[i]));
        }

        barChart.getData().add(series);
    }
    
	@Override
	protected String getViewTitle() {
		return "Statistics";
	}

}
