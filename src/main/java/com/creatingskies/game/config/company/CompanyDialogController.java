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

import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.util.Util;

public class CompanyDialogController  {

	@FXML private TextField companyNameField;
	
	private Stage stage;
	private Company company;
	private boolean isSaveButtonClicked;
	
	public boolean show(Company company) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("CompanyDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        stage = new Stage();
	        stage.setTitle("Company");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        CompanyDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.setCompany(company);

	        stage.showAndWait();
	        return true;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public void setCompany(Company company) {
        this.company = company;
        companyNameField.setText(company.getName());
    }
	
	public void setStage(Stage stage) {
        this.stage = stage;
    }
	
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            company.setName(companyNameField.getText());
            new CompanyDAO().saveOrUpdate(company);
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
    	if(Util.isBlank(companyNameField.getText())){
    		new AlertDialog(AlertType.ERROR, "Invalid Field", null, "Company name is required.", stage).showAndWait();
    		return false;
    	}
    	return true;
    }

    public boolean isSaveButtonClicked() {
		return isSaveButtonClicked;
	}
}
