package com.creatingskies.game.map;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.model.IRecord;

public class MapController extends TableViewController{

	@FXML private TableView<Map> mapsTable;
	@FXML private TableColumn<Map, String> nameColumn;
	@FXML private TableColumn<Map, String> descriptionColumn;
	@FXML private TableColumn<Map, String> widthColumn;
	@FXML private TableColumn<Map, String> heightColumn;
	@FXML private TableColumn<Map, Object> actionColumn;
	
	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();;
		MapDao mapDao = new MapDao();
		
		nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
		widthColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				String.valueOf(cellData.getValue().getWidth())));
		heightColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				String.valueOf(cellData.getValue().getHeight())));
		
		actionColumn.setCellFactory(generateCellFactory(Action.EDIT, Action.VIEW));
		mapsTable.setItems(FXCollections.observableArrayList(mapDao.findAllMaps()));
	}
	
	@FXML
	private void createNew(){
		new MapPropertiesController().show(Action.ADD, new Map());
	}
	
	@Override
	protected void viewRecord(IRecord record) {
		Map map = new MapDao().findMapWithDetails(record.getIdNo());
		new MapPropertiesController().show(Action.VIEW, map);
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Map.fxml"));
            AnchorPane maps = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(maps);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return mapsTable;
	}

	@Override
	protected String getViewTitle() {
		return "Maps";
	}

}
