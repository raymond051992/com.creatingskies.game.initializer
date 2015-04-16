package com.creatingskies.game.config.company;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;
import com.creatingskies.game.util.Util;

public class PlayerDialogController  {

	@FXML private TextField playerNameField;
	@FXML private Label currentTeam;
	@FXML private ComboBox<Team> teamComboBox;
	
	private Stage stage;
	private Player player;
	private boolean isSaveButtonClicked;
	
	public boolean show(Player player) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("PlayerDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        stage = new Stage();
	        stage.setTitle("Player");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        PlayerDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.setPlayer(player);
	        controller.init();
	        
	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void init(){
		currentTeam.setText(player.getTeam().getTeamName());
		List<Team> teams = new CompanyDAO().findAllTeamByCompany(player.getTeam().getGroup().getCompany().getIdNo());
		teamComboBox.getItems().clear();
		teamComboBox.getItems().add(null);
		if(teams != null && !teams.isEmpty()){
			for(Team team : teams){
				if(team.getIdNo() != player.getTeam().getIdNo()){
					teamComboBox.getItems().add(team);
				}
			}
		}
		
		teamComboBox.setConverter(new StringConverter<Team>(){
	        @Override 
	        public String toString(Team team) {

	            if (team==null) {
	                return "";
	            }
	            return team.getTeamName();
	        }

	        @Override 
	        public Team fromString(String string) {
	            return null;
	        }

	    });
	}

	public void setPlayer(Player player) {
        this.player = player;
        playerNameField.setText(player.getName());
    }
	
	public void setStage(Stage stage) {
        this.stage = stage;
    }
	
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            player.setName(playerNameField.getText());
            
            if(teamComboBox.getSelectionModel() != null && teamComboBox.getSelectionModel().getSelectedItem() != null){
            	player.setTeam(teamComboBox.getSelectionModel().getSelectedItem());
            }
            
            new CompanyDAO().saveOrUpdate(player);
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
    	if(Util.isBlank(playerNameField.getText())){
    		new AlertDialog(AlertType.ERROR, "Invalid Field", null, "Player name is required.", stage).showAndWait();
    		return false;
    	}
    	return true;
    }

    public boolean isSaveButtonClicked() {
		return isSaveButtonClicked;
	}
}
