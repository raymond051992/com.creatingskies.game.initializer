package com.creatingskies.game.config.obstacle;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.obstacle.Obstacle;
import com.creatingskies.game.model.obstacle.ObstacleDAO;

public class ObstaclesController extends TableViewController{

	@FXML private TableView<Obstacle> obstaclesTable;
	@FXML private TableColumn<Obstacle, String> nameColumn;
	@FXML private TableColumn<Obstacle, String> difficultyColumn;
	@FXML private TableColumn<Obstacle, String> radiusColumn;
	@FXML private TableColumn<Obstacle, String> gameTypeColumn;
	@FXML private TableColumn<Obstacle, Object> imageColumn;
	@FXML private TableColumn<Obstacle, Object> actionColumn;
	
	private ObstacleDAO obstacleDAO;
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Obstacles.fxml"));
            AnchorPane obstacles = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(obstacles);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		obstacleDAO = new ObstacleDAO();
		
		nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getName()));
		
		difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getDifficulty() != null ? cellData
						.getValue().getDifficulty().toString() : null));
		
		radiusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getRadius() != null ? cellData
						.getValue().getRadius().toString() : null));
		
		gameTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
						getGameTypeDisplay(cellData.getValue())));
		
		imageColumn.setCellFactory(generateImageCellFactory());
		actionColumn.setCellFactory(generateCellFactory(Action.EDIT, Action.DELETE));
		resetTableView();
	}
	
	private Callback<TableColumn<Obstacle, Object>, TableCell<Obstacle, Object>> generateImageCellFactory(){
		Callback<TableColumn<Obstacle, Object>, TableCell<Obstacle, Object>> 
		actionColumnCellFactory =  new Callback<TableColumn<Obstacle, Object>, TableCell<Obstacle, Object>>() {
	        @Override
	        public TableCell<Obstacle, Object> call(final TableColumn<Obstacle,Object> param) {
				final TableCell<Obstacle, Object> cell = new TableCell<Obstacle, Object>() {
					@Override
					public void updateItem(Object item, boolean empty) {
						if(!empty){
							param.getTableView().getSelectionModel().select(getIndex());
							Obstacle obstacle = getTableView().getSelectionModel().getSelectedItem();
							if (obstacle != null) {
								VBox box = new VBox();
								box.setAlignment(Pos.CENTER);
								Label filename = new Label(obstacle.getImageFileName());
								ImageView imgView = new ImageView(Util.byteArrayToImage(obstacle.getImage()));
								imgView.setFitHeight(Constant.TILE_HEIGHT);
								imgView.setFitWidth(Constant.TILE_WIDTH);
								box.getChildren().addAll(imgView, filename);
								setGraphic(box);
							}
						}
					}
				};
				return cell;
			}
		};
		
		return actionColumnCellFactory;
	}
	
	private String getGameTypeDisplay(Obstacle obstacle){
		String displayString = "";
		
		if(obstacle.getForRowing()){
			displayString += Type.ROWING.toString() + ". ";
		}
		
		if(obstacle.getForCycling()){
			displayString += Type.CYCLING.toString() + ". ";
		}
		
		return displayString;
	}
	
	private void resetTableView(){
		obstaclesTable.setItems(FXCollections
				.observableArrayList(obstacleDAO.findAll()));
	}
	
	@Override
	protected String getViewTitle() {
		return "Obstacles";
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return obstaclesTable;
	}
	
	@FXML
	private void handleAdd() {
		new ObstaclePropertiesController().show(Action.ADD, new Obstacle());
	}

	@Override
	protected void editRecord(IRecord record) {
		super.editRecord(record);
		
		if(record instanceof Obstacle){
			new ObstaclePropertiesController().show(Action.ADD, (Obstacle) record);
		}
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
				"Are you sure you want to delete this obstacle?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			super.deleteRecord(record);
			try {
				obstacleDAO.delete(record);
				resetTableView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
