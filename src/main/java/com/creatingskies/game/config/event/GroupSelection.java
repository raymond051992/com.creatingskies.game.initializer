package com.creatingskies.game.config.event;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.GameCoreController;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;
import com.creatingskies.game.model.event.GameEvent;

public class GroupSelection extends AnchorPane{

	private final double VBOX_SPACING = 10;
	private final double WIDTH = 300;
	private final double HEIGHT = 400;
	private final double SCROLL_WIDTH = 300;
	private final double SCROLL_HEIGHT = 300;
	
	private ScrollPane scrollPane = new ScrollPane();
	private AnchorPane infoBoxContainer = new AnchorPane();
	private VBox infoBox = new VBox(VBOX_SPACING);
	
	public GroupSelection(final Group group,final GameEvent gameEvent,GameEventGroupSelectionController controller) {
		initStyle();
		
		Label groupNameLabel = new Label(group.getName());
		infoBox.getChildren().add(groupNameLabel);
		
		for(Team team : getTeams(group)){
			Label teamNameLabel = new Label(team.getName());
			teamNameLabel.setPadding(new Insets(0, 0, 0, 10));
			infoBox.getChildren().add(teamNameLabel);
			
			for(Player player : getPlayers(team)){
				Label playerNameLabel = new Label(player.getName());
				playerNameLabel.setPadding(new Insets(0, 0, 0, 20));
				infoBox.getChildren().add(playerNameLabel);
			}
		}
		infoBoxContainer.getChildren().add(infoBox);
		scrollPane.setContent(infoBoxContainer);
		
		getChildren().add(scrollPane);
		
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER);
		Button startGameButton = new Button("START GAME");
		startGameButton.setMinHeight(50);
		startGameButton.setStyle("-fx-font-size:20px;-fx-font-weight:bold;");
		
		startGameButton.setOnAction((event) -> {
			controller.stage.close();
			Alert waitDialog = new AlertDialog(AlertType.INFORMATION, "Loading map", null, "Please wait.");
	    	waitDialog.initModality(Modality.WINDOW_MODAL);
			waitDialog.show();
			new GameCoreController().show(gameEvent);
			waitDialog.hide();
		});
		
		buttonContainer.getChildren().add(startGameButton);
		
		AnchorPane.setBottomAnchor(buttonContainer, 10D);
		AnchorPane.setLeftAnchor(buttonContainer, 10D);
		AnchorPane.setRightAnchor(buttonContainer, 10D);
		
		
		getChildren().add(buttonContainer);
	}
	
	private List<Team> getTeams(final Group group){
		return new CompanyDAO().findAllTeamsForGroup(group);
	}
	
	private List<Player> getPlayers(final Team team){
		return new CompanyDAO().findAllPlayersForTeam(team);
	}
	
	private void initStyle(){
		setStyle("-fx-border-color:#000000");
		setMinSize(WIDTH, HEIGHT);
		setPrefSize(WIDTH, HEIGHT);
		setMaxSize(WIDTH, HEIGHT);
		
		scrollPane.setMinSize(SCROLL_WIDTH, SCROLL_HEIGHT);
		scrollPane.setPrefSize(SCROLL_WIDTH, SCROLL_HEIGHT);
		scrollPane.setMaxSize(SCROLL_WIDTH, SCROLL_HEIGHT);
		scrollPane.setHmax(1);
		scrollPane.setVmax(1);
		
		infoBoxContainer.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		
		AnchorPane.setLeftAnchor(scrollPane, 10D);
		AnchorPane.setRightAnchor(scrollPane, 10D);
		AnchorPane.setTopAnchor(scrollPane, 10D);
		
		AnchorPane.setBottomAnchor(infoBox, 0D);
		AnchorPane.setLeftAnchor(infoBox, 0D);
		AnchorPane.setRightAnchor(infoBox, 0D);
		AnchorPane.setTopAnchor(infoBox, 0D);
	}
}
