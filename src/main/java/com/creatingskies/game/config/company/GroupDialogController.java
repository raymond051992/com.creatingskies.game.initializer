package com.creatingskies.game.config.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.component.TableRowDeleteButton;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Team;
import com.creatingskies.game.util.Util;

public class GroupDialogController  {
	
	private final int MAX_TEAM_PER_GROUP = 2;
	
	@FXML private VBox teamsContainer;
	@FXML private TextField groupNameField;
	@FXML private Button addTeamButton;
	
	private Stage stage;
	private Group group;
	private boolean isSaveButtonClicked;
	private List<Team> teamsToBeRemove = new ArrayList<Team>();
	
	public boolean show(Group group) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("GroupDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
	        
	        stage = new Stage();
	        stage.setTitle("Group");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UTILITY);
	        stage.setResizable(false);
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        GroupDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.setGroup(group);
	        controller.init();
	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void init(){
		teamsToBeRemove.clear();
		groupNameField.setText(group.getName());
		initTeamList();
		if(group.getTeams().size() == MAX_TEAM_PER_GROUP){
			addTeamButton.setVisible(false);
		}else{
			addTeamButton.setVisible(true);
		}
	}
	
	public void addTeam(){
		Team team = new Team();
		team.setGroup(group);
		team.setName("");
		group.getTeams().add(team);
		teamsContainer.getChildren().add(teamRow(team));
		if(group.getTeams().size() == MAX_TEAM_PER_GROUP){
			addTeamButton.setVisible(false);
		}else{
			addTeamButton.setVisible(true);
		}
	}
	
	private GridPane teamRow(Team team){
		GridPane container = new GridPane();
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        container.getColumnConstraints().addAll(col1,col2);
        
        TextField teamName = new TextField(team.getName());
        TableRowDeleteButton deleteButton = new TableRowDeleteButton();
        
        teamName.setPrefWidth(Integer.MAX_VALUE);
        teamName.textProperty().addListener((observable, oldValue, newValue) -> {
        	if(newValue == null || (newValue != null && newValue.isEmpty())){
        		team.setName(oldValue);
        	}else{
        		team.setName(newValue);
        	}
		});
        
        deleteButton.setOnAction((event)->{
        	teamsToBeRemove.add(team);
        	initTeamList();
        });
        
        GridPane.setConstraints(teamName, 0, 0);
		GridPane.setConstraints(deleteButton, 1, 0);
		
		container.getChildren().addAll(teamName,deleteButton);
		
        return container;
	}
	
	private void initTeamList(){
		teamsContainer.getChildren().clear();
		if(group.getTeams() != null && !group.getTeams().isEmpty()){
			for(Team team : group.getTeams()){
				if(!teamsToBeRemove.contains(team)){
					teamsContainer.getChildren().add(teamRow(team));
				}
			}
		}
	}

	public void setGroup(Group group) {
        this.group = group;
        groupNameField.setText(group.getName());
    }
	
	public void setStage(Stage stage) {
        this.stage = stage;
    }
	
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
        	CompanyDAO companyDAO = new CompanyDAO();
            group.setName(groupNameField.getText());
            if(group.getTeams() != null && !group.getTeams().isEmpty()){
	    		for(Team team : group.getTeams()){
	    			if(team.getName() == null || (team.getName() != null && team.getName().isEmpty())){
	    				teamsToBeRemove.add(team);
	    			}
	    		}
            }
            
            companyDAO.saveOrUpdate(group);
            
            for(Team team : teamsToBeRemove){
            	if(team.getIdNo() != null){
            		try {
    					companyDAO.delete(team);
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
            	}
            }
            
            teamsToBeRemove.clear();
            isSaveButtonClicked = true;
            stage.close();
        }
    }
	
    @FXML
    private void handleCancel() {
    	isSaveButtonClicked = false;
        stage.close();
    }

    private boolean isInputValid() {
    	if(Util.isBlank(groupNameField.getText())){
    		new AlertDialog(AlertType.ERROR, "Invalid Field", null, "Group name is required.", stage).showAndWait();
    		return false;
    	}
    	return true;
    }

    public boolean isSaveButtonClicked() {
		return isSaveButtonClicked;
	}
}
