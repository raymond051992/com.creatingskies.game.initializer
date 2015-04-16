package com.creatingskies.game.config.company;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import com.creatingskies.game.component.TableRowEditButton;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;

public class CompanyGroupPane extends AnchorPane{

	private Group group;
	
	VBox mainContainer = new VBox(10);
	
	private GridPane createGroupRow(){
		GridPane container = new GridPane();
		
		Label groupName = new Label(group.getName());
		TableRowEditButton editButton = new TableRowEditButton();
		
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        container.getColumnConstraints().addAll(col1,col2);
        
        groupName.setPrefWidth(Integer.MAX_VALUE);
        groupName.setStyle("-fx-font-weight:bold;-fx-font-size:14px;");
        editButton.setTooltip(new Tooltip("Edit Group and Teams"));
        
        editButton.setOnAction((event) -> {
        	if(new GroupDialogController().show(group)){
        		this.group = new CompanyDAO().findGroup(group.getIdNo());
        		groupName.setText(group.getName());
        		init();
        	};
        });
        
		GridPane.setConstraints(groupName, 0, 0);
		GridPane.setConstraints(editButton, 1, 0);
		
		container.getChildren().addAll(groupName,editButton);
		return container;
	}
	
	private GridPane createTeamRow(final Team team){
		GridPane container = new GridPane();
		
		Label teamName = new Label(team.getName());
		teamName.setPadding(new Insets(0, 0, 0, 10));
		TableRowEditButton editButton = new TableRowEditButton();
		
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        
        container.getColumnConstraints().addAll(col1,col2);
        teamName.setPrefWidth(Integer.MAX_VALUE);
        teamName.setStyle("-fx-font-weight:bold;");
        editButton.setTooltip(new Tooltip("Edit Team and Players"));
        
        editButton.setOnAction((event) -> {
        	if(new TeamDialogController().show(team)){
        		Team theTeam = group.getTeams().get(group.getTeams().indexOf(team));
        		theTeam = new CompanyDAO().findTeam(team.getIdNo());
        		teamName.setText(theTeam.getName());
        		init();
        	};
        });
        
		GridPane.setConstraints(teamName, 0, 0);
		GridPane.setConstraints(editButton, 1, 0);
		
		container.getChildren().addAll(teamName,editButton);
		return container;
	}
	
	
	private GridPane createPlayerRow(final Player player){
		final Team team = player.getTeam();
		GridPane container = new GridPane();
		
		Label playerName = new Label(player.getName());
		TableRowEditButton editButton = new TableRowEditButton();
		
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        
        container.getColumnConstraints().addAll(col1,col2);
        playerName.setPrefWidth(Integer.MAX_VALUE);
        playerName.setPadding(new Insets(0, 0, 0, 20));
        editButton.setTooltip(new Tooltip("Edit " + player.getName()));
        
        editButton.setOnAction((event) -> {
        	if(new PlayerDialogController().show(player)){
        		Player thePlayer = team.getPlayers().get(team.getPlayers().indexOf(player));
        		playerName.setText(thePlayer.getName());
        		init();
        	};
        });
        
		GridPane.setConstraints(playerName, 0, 0);
		GridPane.setConstraints(editButton, 1, 0);
		
		container.getChildren().addAll(playerName,editButton);
		return container;
	}
	
	private void initTeams(){
		for(Team team : group.getTeams()){
			VBox teamContainer = new VBox(10);
			
			teamContainer.getChildren().add(createTeamRow(team));
			Team t = new CompanyDAO().findTeam(team.getIdNo());
			for(Player player : t.getPlayers()){
				teamContainer.getChildren().add(createPlayerRow(player));
			}
			
			mainContainer.getChildren().add(teamContainer);
		}
	}
	
	private void init(){
		mainContainer.getChildren().clear();
		mainContainer.getChildren().add(createGroupRow());
		initTeams();
	}
	
	public CompanyGroupPane(Group group) {
		this.group = group;
		
		AnchorPane.setLeftAnchor(mainContainer, 10D);
		AnchorPane.setTopAnchor(mainContainer, 10D);
		AnchorPane.setRightAnchor(mainContainer, 10D);
		AnchorPane.setBottomAnchor(mainContainer, 10D);
		
		init();
		
		
		this.getChildren().add(mainContainer);
		this.setMinSize(300, 400);
		this.setPrefSize(300, 400);
		this.setStyle("-fx-border-color:#000000;");
	}
}

