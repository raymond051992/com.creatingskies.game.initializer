package com.creatingskies.game.config.event;

import java.util.List;

import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.event.GameEvent;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GameEventGroupSelectionController {

	private Stage stage;
	private FlowPane groupSelectionContainer = new FlowPane();
	
	
	public void show(final GameEvent gameEvent){
		Scene scene = new Scene(new AnchorPane(),800,600);
		AnchorPane sceneRoot = (AnchorPane) scene.getRoot();
		
		sceneRoot.getStylesheets().add("/css/dialog.css");
		sceneRoot.getStylesheets().add("/css/style.css");
		sceneRoot.getStyleClass().add("background");
		
		stage = new Stage();
        stage.setTitle("Select a Group");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(MainLayout.getPrimaryStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setScene(scene);
        
        
        AnchorPane.setBottomAnchor(groupSelectionContainer, 10D);
		AnchorPane.setLeftAnchor(groupSelectionContainer, 10D);
		AnchorPane.setRightAnchor(groupSelectionContainer, 10D);
		AnchorPane.setTopAnchor(groupSelectionContainer, 10D);
        
        groupSelectionContainer.setOrientation(Orientation.HORIZONTAL);
        for(Group group : getAllGroups(gameEvent.getCompany())){
        	groupSelectionContainer.getChildren().add(new GroupSelection(group,gameEvent,this));
        }
        
        sceneRoot.getChildren().add(groupSelectionContainer);
        
        stage.showAndWait();
	}
	
	private List<Group> getAllGroups(final Company company){
		return new CompanyDAO().findAllGroupsForCompany(company,false);
	}

	public Stage getStage() {
		return stage;
	}
}
