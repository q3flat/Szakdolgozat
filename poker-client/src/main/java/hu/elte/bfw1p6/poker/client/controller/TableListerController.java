package hu.elte.bfw1p6.poker.client.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ResourceBundle;

import hu.elte.bfw1p6.poker.client.controller.main.CommunicatorController;
import hu.elte.bfw1p6.poker.client.controller.main.FrameController;
import hu.elte.bfw1p6.poker.client.controller.main.PokerClientController;
import hu.elte.bfw1p6.poker.client.controller.main.PokerObserverController;
import hu.elte.bfw1p6.poker.client.model.Model;
import hu.elte.bfw1p6.poker.client.model.helper.ConnectTableHelper;
import hu.elte.bfw1p6.poker.exception.PokerDataBaseException;
import hu.elte.bfw1p6.poker.exception.PokerInvalidSession;
import hu.elte.bfw1p6.poker.exception.PokerUnauthenticatedException;
import hu.elte.bfw1p6.poker.model.entity.PokerTable;
import hu.elte.bfw1p6.poker.model.entity.PokerType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableListerController implements PokerClientController, Initializable, PokerObserverController {

	private final String NO_TABLE_SELECTED_MESSAGE = "Nem választottál ki egy táblát sem!";
	private final String SUCC_TABLE_DELETE_MSG = "Sikeresen kitörölted a táblát!";

	private FrameController frameController;
	private CommunicatorController commCont;

	@FXML private TableView<PokerTable> tableView;

	@FXML private TableColumn<PokerTable, String> tableName;
	@FXML private TableColumn<PokerTable, PokerType> pokerType;
	@FXML private TableColumn<PokerTable, Integer> maxTime;
	@FXML private TableColumn<PokerTable, Integer> maxPlayers;
	@FXML private TableColumn<PokerTable, BigDecimal> defaultPot;
	@FXML private TableColumn<PokerTable, BigDecimal> maxBet;

	@FXML private Button connectButton;
	@FXML private Button createTableButton;
	@FXML private Button logoutButton;
	@FXML private Button modifyTableButton;
	@FXML private Button deleteTableButton;
	@FXML private Button profileManagerButton;
	@FXML private Button viewUsersbutton;


	private Alert alert;

	private Model model;

	public TableListerController() {
		try {
			commCont = new CommunicatorController(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model = Model.getInstance();
		alert = new Alert(AlertType.ERROR);
	}

	@Override
	public void setDelegateController(FrameController frameController) {
		this.frameController = frameController;
		try {
			List<PokerTable> tables = model.registerTableViewObserver(commCont);
			tableView.getItems().setAll(tables);
		} catch (RemoteException | PokerDataBaseException | PokerUnauthenticatedException e) {
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tableName.setCellValueFactory(new PropertyValueFactory<PokerTable, String>("name"));
		pokerType.setCellValueFactory(new PropertyValueFactory<PokerTable, PokerType>("pokerType"));
		maxTime.setCellValueFactory(new PropertyValueFactory<PokerTable, Integer>("maxTime"));
		maxPlayers.setCellValueFactory(new PropertyValueFactory<PokerTable, Integer>("maxPlayers"));
		defaultPot.setCellValueFactory(new PropertyValueFactory<PokerTable, BigDecimal>("defaultPot"));
		maxBet.setCellValueFactory(new PropertyValueFactory<PokerTable, BigDecimal>("maxBet"));

		try {
			if(!model.isAdmin()) {
				createTableButton.setVisible(false);
				modifyTableButton.setVisible(false);
				deleteTableButton.setVisible(false);
			}
		} catch (RemoteException | PokerUnauthenticatedException | PokerDataBaseException e) {
			showAlert(e.getMessage());
		}
	}

	@FXML
	protected void handleConnectToTable() {
		PokerTable table = getSelectedPokerTable();
		if (table == null) {
			alert.setContentText(NO_TABLE_SELECTED_MESSAGE);
			alert.showAndWait();
		} else {
			ConnectTableHelper.getInstance().setPokerTable(table);
			removeObserver();
			if(table.getPokerType() == PokerType.HOLDEM) {
				frameController.setHoldemMainGameFXML();
			} else if (table.getPokerType() == PokerType.CLASSIC) {
				frameController.setClassicMainGameFXML();
			}
		}
	}

	private PokerTable getSelectedPokerTable() {
		return tableView.getSelectionModel().getSelectedItem();
	}

	@FXML
	protected void handleCreateTable() {
		removeObserver();
		frameController.setCreateTableFXML();
	}

	@FXML
	protected void handleModifyTable() {
		PokerTable selectedPokerTable = getSelectedPokerTable();
		if (selectedPokerTable != null) {
			model.setParameterPokerTable(selectedPokerTable);
			frameController.setCreateTableFXML();
		} else {
			showAlert(NO_TABLE_SELECTED_MESSAGE);
		}
	}

	@FXML
	protected void handleDeleteTable() {
		PokerTable selectedPokerTable = getSelectedPokerTable();
		if (selectedPokerTable != null) {
			try {
				model.deleteTable(selectedPokerTable);
				alert.setAlertType(AlertType.INFORMATION);
				alert.setContentText(SUCC_TABLE_DELETE_MSG);
				alert.showAndWait();
				alert.setAlertType(AlertType.ERROR);
			} catch (RemoteException | PokerDataBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PokerUnauthenticatedException e) {
				showAlert(e.getMessage());
			}
		} else {
			showAlert(NO_TABLE_SELECTED_MESSAGE);
		}
	}

	@FXML
	protected void handleLogout() {
		try {
			model.logout();
			frameController.setLoginFXML();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PokerInvalidSession e) {
			showAlert(e.getMessage());
		}
	}

	@FXML
	protected void handleProfile() {
		frameController.setProfileManagerFXML();
	}
	
	@FXML
	protected void handleViewUsers() {
		frameController.setUsersFXML();
	}

	@FXML
	protected void handleProfileManager() {
		frameController.setProfileManagerFXML();
	}

	private void removeObserver() {
		try {
			model.removeTableViewObserver(commCont);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showAlert(String msg) {
		alert.setContentText(msg);
		alert.showAndWait();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void updateMe(Object updateMsg) {
		List<PokerTable> tables = (List<PokerTable>)updateMsg;
		tableView.getItems().setAll(tables);
		System.out.println("MEGKAPTAM A TÁBLÁKAT");
	}
}