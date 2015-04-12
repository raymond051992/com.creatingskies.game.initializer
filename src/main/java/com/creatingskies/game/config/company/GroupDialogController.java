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
import com.creatingskies.game.model.company.Group;

public class GroupDialogController  {

	@FXML private TextField groupNameField;
	
	private Stage stage;
	private Group group;
	private boolean isSaveButtonClicked;
	
	public boolean show(Group group) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("GroupDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        stage = new Stage();
	        stage.setTitle("Group");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        GroupDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.setGroup(group);

	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
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
            group.setName(groupNameField.getText());
            new CompanyDAO().saveOrUpdate(group);
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
