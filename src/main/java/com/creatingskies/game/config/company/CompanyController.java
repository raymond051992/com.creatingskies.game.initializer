package com.creatingskies.game.config.company;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.classes.TableViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.IRecord;
import com.creatingskies.game.model.company.Company;
import com.creatingskies.game.model.company.CompanyDAO;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.company.Player;
import com.creatingskies.game.model.company.Team;

public class CompanyController extends TableViewController{

	@FXML private TableView<Company> companiesTable;
	@FXML private TableColumn<Company, String> companyNameColumn;
	@FXML private TableColumn<Company, Object> companyActionColumn;
	
	@FXML private TableView<Group> groupsTable;
	@FXML private TableColumn<Group, String> groupNameColumn;
	@FXML private TableColumn<Group, Object> groupActionColumn;
	
	@FXML private TableView<Team> teamsTable;
	@FXML private TableColumn<Team, String> teamNameColumn;
	@FXML private TableColumn<Team, Object> teamActionColumn;
	
	@FXML private TableView<Player> playersTable;
	@FXML private TableColumn<Player, String> playerNameColumn;
	@FXML private TableColumn<Player, Object> playerActionColumn;
	
	@FXML private Button addGroupButton;
	@FXML private Button addTeamButton;
	@FXML private Button addPlayerButton;
	
	private CompanyDAO companyDAO;
	
	private Company selectedCompany;
	private Group selectedGroup;
	private Team selectedTeam;
	private Player selectedPlayer;
	
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
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	@FXML
	@SuppressWarnings("unchecked")
	public void initialize(){
		super.init();
		companyDAO = new CompanyDAO();
		
		
		addGroupButton.setVisible(false);
		addTeamButton.setVisible(false);
		addPlayerButton.setVisible(false);
		
		companyNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getName()));
		companyActionColumn.setCellFactory(generateCellFactory(Action.EDIT, Action.DELETE));
		
		groupNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
				cellData.getValue().getName()));

		groupActionColumn.setCellFactory(generateCellFactory(groupsTable,
				Action.EDIT, Action.DELETE));
		
		teamNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		
		teamActionColumn.setCellFactory(generateCellFactory(teamsTable,
				Action.EDIT, Action.DELETE));
		
		playerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
		
		playerActionColumn.setCellFactory(generateCellFactory(playersTable,
				Action.EDIT, Action.DELETE));
		
		companiesTable.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> loadCompanyDetails(newValue));
		
		groupsTable.getSelectionModel().selectedItemProperty()
			.addListener((observable, oldValue, newValue) -> loadGroupDetails(newValue));
		
		teamsTable.getSelectionModel().selectedItemProperty()
		.addListener((observable, oldValue, newValue) -> loadTeamDetails(newValue));
		
		playersTable.getSelectionModel().selectedItemProperty()
		.addListener((observable, oldValue, newValue) -> loadPlayerDetails(newValue));
		
		resetTableView();
	}
	
	private void loadCompanyDetails(Company company) {
		selectedCompany = company;
		resetGroupTableView();
		addGroupButton.setVisible(true);
	}
	
	private void loadGroupDetails(Group group){
		selectedGroup = group;
		resetTeamTableView();
		addTeamButton.setVisible(true);
	}
	
	private void loadTeamDetails(Team team){
		selectedTeam = team;
		resetPlayerTableView();
		addPlayerButton.setVisible(true);
	}
	
	private void loadPlayerDetails(Player player){
		selectedPlayer = player;
	}
	
	private void resetTableView(){
		companiesTable.setItems(FXCollections.observableArrayList(companyDAO
				.findAllCompanies()));
	}
	
	private void resetGroupTableView(){
		groupsTable.setItems(FXCollections.observableArrayList(
				companyDAO.findAllGroupsForCompany(selectedCompany)));
	}
	
	private void resetTeamTableView(){
		teamsTable.setItems(
				FXCollections.observableArrayList(
						companyDAO.findAllTeamsForGroup(selectedGroup)));
	}
	
	private void resetPlayerTableView(){
		playersTable.setItems(
				FXCollections.observableArrayList(
						companyDAO.findAllPlayersForTeam(selectedTeam)));
	}
	
	@FXML
	private void handleAdd() {
		if(new CompanyDialogController().show(new Company())){
			resetTableView();
		}
	}
	
	@FXML
	private void addGroup(){
		Group group = new Group();
		group.setCompany(selectedCompany);
		if(new GroupDialogController().show(group)){
			resetGroupTableView();
		}
	}
	
	@FXML
	private void addTeam(){
		Team team = new Team();
		team.setGroup(selectedGroup);
		if(new TeamDialogController().show(team)){
			resetTeamTableView();
		}
	}
	
	@FXML
	private void addPlayer(){
		Player player = new Player();
		player.setTeam(selectedTeam);
		if(new PlayerDialogController().show(player)){
			resetPlayerTableView();
		}
	}
	
	@Override
	public TableView<? extends IRecord> getTableView() {
		return companiesTable;
	}
	
	@Override
	protected void deleteRecord(IRecord record) {
		super.deleteRecord(record);
		Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
				"Are you sure you want to delete this record?", null).showAndWait();
		
		if(result.get() == ButtonType.OK){
			try {
				companyDAO.delete(record);
				resetTableView();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void editRecord(IRecord record) {
		if(new CompanyDialogController().show((Company)record)){
			resetTableView();
		}
	}
	
}
