package com.creatingskies.game.config.company;

import java.io.IOException;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Team;

public class CompanyController extends TableViewController{

	@FXML private TableView<Company> companyTableView;
	@FXML private TableColumn<Company, String> companyNameTableColumn;
	@FXML private TableColumn<Company, Object> companyActionTableColumn;
	
	@FXML private FlowPane groupsFlowPane;
	@FXML private Button addGroupButton;
	
	private Company selectedCompany;
	
	@Override
	public void init() {
		super.init();
		initTable();
		addGroupButton.setVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	private void initTable(){
		companyNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getName()));
		
		companyActionTableColumn.setCellFactory(generateCellFactory(Action.EDIT));
		
		companyTableView.setItems(FXCollections.observableArrayList(new CompanyDAO().findAllCompanies()));
		
		companyTableView.getSelectionModel().selectedItemProperty()
    	.addListener(
    			(observable, oldValue, newValue) -> showDetails(newValue));
		
	}
	
	public void addNewGroup(){
		Group group = new Group();
		group.setCompany(selectedCompany);
		if(new GroupDialogController().show(group)){
			showDetails(selectedCompany);
		}
	}
	
	public void showDetails(Company company){
		groupsFlowPane.getChildren().clear();
		List<Group> groups = new CompanyDAO().findAllGroupsForCompany(company);
		for(Group group : groups){
			group.setTeams(new CompanyDAO().findAllTeamsForGroup(group));
			for(Team team : group.getTeams()){
				team.setPlayers(new CompanyDAO().findAllPlayersForTeam(team));
			}
			groupsFlowPane.getChildren().add(new CompanyGroupPane(group));
		}
		selectedCompany = company;
		addGroupButton.setVisible(true);
	}
	
	@Override
	protected String getViewTitle() {
		return "Companies";
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Company.fxml"));
            AnchorPane companies = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(companies);
            
            CompanyController controller = loader.getController();
            controller.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@Override
	public TableView<? extends IRecord> getTableView() {
		return companyTableView;
	}
}
