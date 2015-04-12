package com.creatingskies.game.config.company;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Team;

public class TeamDialogController  {

	@FXML private TextField teamNameField;
	
	private Stage stage;
	private Team team;
	private boolean isSaveButtonClicked;
	
	public boolean show(Team team) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("TeamDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        stage = new Stage();
	        stage.setTitle("Team");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        TeamDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.setTeam(team);

	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
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
            team.setName(teamNameField.getText());
            new CompanyDAO().saveOrUpdate(team);
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
