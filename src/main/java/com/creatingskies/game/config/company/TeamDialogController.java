package com.creatingskies.game.config.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
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
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;
import com.creatingskies.game.util.Util;

public class TeamDialogController  {

	@FXML private TextField teamNameField;
	@FXML private VBox playersContainer;
	
	private Stage stage;
	private Team team;
	private boolean isSaveButtonClicked;
	private List<Player> playersToBeRemove = new ArrayList<Player>();
	
	public boolean show(Team team) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("TeamDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
	        
	        stage = new Stage();
	        stage.setTitle("Team");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UTILITY);
	        stage.setResizable(false);
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        TeamDialogController controller = loader.getController();
	        team = new CompanyDAO().findTeam(team.getIdNo());
	        controller.setStage(stage);
	        controller.setTeam(team);
	        controller.init();
	        
	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void init(){
		playersToBeRemove.clear();
		teamNameField.setText(getTeam().getName());
		initPlayerList();
	}
	
	public void addPlayer(){
		Player player = new Player();
		player.setTeam(getTeam());
		player.setName("");
		getTeam().getPlayers().add(player);
		playersContainer.getChildren().add(playerRow(player));
	}
	
	public Team getTeam() {
		return team;
	}
	
	private GridPane playerRow(Player player){
		GridPane container = new GridPane();
		ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        container.getColumnConstraints().addAll(col1,col2);
        
        TextField playerName = new TextField(player.getName());
        TableRowDeleteButton deleteButton = new TableRowDeleteButton();
        
        playerName.setPrefWidth(Integer.MAX_VALUE);
        playerName.textProperty().addListener((observable, oldValue, newValue) -> {
        	if(newValue == null || (newValue != null && newValue.isEmpty())){
        		player.setName(oldValue);
        	}else{
        		player.setName(newValue);
        	}
		});
        
        deleteButton.setOnAction((event)->{
        	playersToBeRemove.add(player);
        	initPlayerList();
        });
        
        GridPane.setConstraints(playerName, 0, 0);
		GridPane.setConstraints(deleteButton, 1, 0);
		
		container.getChildren().addAll(playerName,deleteButton);
		
        return container;
	}
	
	private void initPlayerList(){
		playersContainer.getChildren().clear();
		if(getTeam().getPlayers() != null && !getTeam().getPlayers().isEmpty()){
			for(Player player : getTeam().getPlayers()){
				if(!playersToBeRemove.contains(player)){
					playersContainer.getChildren().add(playerRow(player));
				}
			}
		}
	}

	public void setTeam(Team team) {
        this.team = team;
        teamNameField.setText(team.getName());
    }
	
	public void setStage(Stage stage) {
        this.stage = stage;
    }
	
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
        	CompanyDAO companyDAO = new CompanyDAO();
            team.setName(teamNameField.getText());
            if(team.getPlayers() != null && !team.getPlayers().isEmpty()){
	    		for(Player player : team.getPlayers()){
	    			if(player.getName() == null || (player.getName() != null && player.getName().isEmpty())){
	    				playersToBeRemove.add(player);
	    			}
	    		}
            }
            
            companyDAO.saveOrUpdate(team);
            
            for(Player player : playersToBeRemove){
            	if(player.getIdNo() != null){
            		try {
    					companyDAO.delete(player);
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
            	}
            }
            
            playersToBeRemove.clear();
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
    	if(Util.isBlank(teamNameField.getText())){
    		new AlertDialog(AlertType.ERROR, "Invalid Field", null, "Team name is required.", stage).showAndWait();
    		return false;
    	}
    	return true;
    }

    public boolean isSaveButtonClicked() {
		return isSaveButtonClicked;
	}
}
