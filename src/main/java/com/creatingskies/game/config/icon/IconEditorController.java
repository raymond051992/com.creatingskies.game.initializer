package com.creatingskies.game.config.icon;

import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.TileImage;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.util.Util;

public class IconEditorController extends TableViewController{

	@FXML private TableView<TileImage> tileImagesTable;
	@FXML private TableColumn<TileImage, String> nameColumn;
	@FXML private TableColumn<TileImage, Object> imageColumn;
	@FXML private TableColumn<TileImage, String> difficultyColumn;
	@FXML private TableColumn<TileImage, Object> actionColumn;
	
	private MapDao mapDao;
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("IconEditor.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		mapDao = new MapDao();
		
		nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getOwner()));
		
		difficultyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getDifficulty() != null ? cellData
						.getValue().getDifficulty().toString() : "N/A"));
		
		actionColumn.setCellFactory(generateCellFactory(Action.EDIT));
		resetTableView();
	}
	
	private Callback<TableColumn<TileImage, Object>, TableCell<TileImage, Object>> generateImageCellFactory(){
		Callback<TableColumn<TileImage, Object>, TableCell<TileImage, Object>> 
		actionColumnCellFactory =  new Callback<TableColumn<TileImage, Object>, TableCell<TileImage, Object>>() {
	        @Override
	        public TableCell<TileImage, Object> call(final TableColumn<TileImage,Object> param) {
				final TableCell<TileImage, Object> cell = new TableCell<TileImage, Object>() {
					@Override
					public void updateItem(Object item, boolean empty) {
						if(!empty){
							param.getTableView().getSelectionModel().select(getIndex());
							TileImage tileImage = getTableView().getSelectionModel().getSelectedItem();
							if (tileImage != null) {
								VBox box = new VBox();
								box.setAlignment(Pos.CENTER);
								Label filename = new Label(tileImage.getFileName());
								ImageView imgView = new ImageView(Util.byteArrayToImage(tileImage.getImage()));
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
	
	private void resetTableView(){
		imageColumn.setCellFactory(generateImageCellFactory());
		tileImagesTable.setItems(FXCollections
				.observableArrayList(mapDao.findAllTileImages(true)));
	}
	
	@Override
	protected String getViewTitle() {
		return "Icon Editor";
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return tileImagesTable;
	}
	
	@Override
	protected void editRecord(IRecord record) {
		super.editRecord(record);

		if(record instanceof TileImage){
			showDetailDialog((TileImage) record);
		}
	}
	
	private void showDetailDialog(TileImage tileImage){
		boolean saveClicked = new TileImageDialogController().show(tileImage);
	    if (saveClicked) {
	        mapDao.saveOrUpdate(tileImage);
	        resetTableView();
	    }
	}
	
}
