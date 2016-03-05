package com.creatingskies.game.config.company;

import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.component.TableRowArchiveButton;
import com.creatingskies.game.component.TableRowDeleteButton;
import com.creatingskies.game.component.TableRowEditButton;
import com.creatingskies.game.component.TableRowRestoreButton;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.model.event.GameEventDao;

public class CompanyGroupPane extends AnchorPane{

	private Group group;
	
	VBox mainContainer = new VBox(10);
	
	private GridPane createGroupRow(){
		GridPane container = new GridPane();
		
		Label groupName = new Label(group.getName());
		TableRowEditButton editButton = new TableRowEditButton();
		TableRowDeleteButton deleteButton = new TableRowDeleteButton();
		TableRowArchiveButton archiveButton = new TableRowArchiveButton();
		TableRowRestoreButton restoreButton = new TableRowRestoreButton();
		
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        ColumnConstraints col3 = new ColumnConstraints();
        container.getColumnConstraints().addAll(col1,col2,col3);
        container.setHgap(5.0);
        groupName.setPrefWidth(Integer.MAX_VALUE);
        groupName.setStyle("-fx-font-weight:bold;-fx-font-size:14px;");
        editButton.setTooltip(new Tooltip("Edit Group and Teams"));
        deleteButton.setTooltip(new Tooltip("Delete Group and Teams"));
        archiveButton.setTooltip(new Tooltip("Move group to archive"));
        restoreButton.setTooltip(new Tooltip("Restore from archive"));
        
        editButton.setOnAction((event) -> {
        	if(new GroupDialogController().show(group)){
        		this.group = new CompanyDAO().findGroup(group.getIdNo());
        		groupName.setText(group.getName());
        		init();
        	};
        });
        
        archiveButton.setOnAction((event) -> {
        	this.group.setArchived(true);
        	CompanyDAO companyDAO = new CompanyDAO();
        	companyDAO.saveOrUpdate(group);
        	init();
        });
        
        restoreButton.setOnAction((event) -> {
        	this.group.setArchived(false);
        	CompanyDAO companyDAO = new CompanyDAO();
        	companyDAO.saveOrUpdate(group);
        	init();
        });
        
        deleteButton.setOnAction((event) -> {
        	List<GameEvent> events = new GameEventDao().findAllGameEventByCompany(group.getCompany()); 
    		
    		if(events == null || (events != null && events.isEmpty())){
    			Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
    					"Are you sure you want to delete this group?", null).showAndWait();
    			
    			if(result.get() == ButtonType.OK){
    				try {
    					new GameDao().delete(group);
    					group = null;
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}else{
    			new AlertDialog(AlertType.ERROR, "Error", "", "You cannot delete this company. The record shows that we have an event for this group's company").showAndWait();
    		}
    		init();
        });
        
		GridPane.setConstraints(groupName, 0, 0);
		GridPane.setConstraints(editButton, 1, 0);
		if(this.group.getArchived() == null || this.group.getArchived() == Boolean.FALSE){
			GridPane.setConstraints(archiveButton, 2, 0);	
		}else{
			GridPane.setConstraints(restoreButton, 3, 0);
		}
		GridPane.setConstraints(deleteButton, 4, 0);
		
		container.getChildren().addAll(groupName,editButton,deleteButton);
		
		if(this.group.getArchived() == null || this.group.getArchived() == Boolean.FALSE){
			container.getChildren().add(archiveButton);
		}else{
			container.getChildren().add(restoreButton);
		}
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

