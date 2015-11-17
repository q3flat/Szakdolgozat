package hu.elte.bfw1p6.poker.client.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import hu.elte.bfw1p6.poker.client.model.Model;
import hu.elte.bfw1p6.poker.exception.PokerDataBaseException;
import hu.elte.bfw1p6.poker.model.entity.PokerTable;
import hu.elte.bfw1p6.poker.model.entity.PokerType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Új poker asztal entitást létrehozó controller.
 * @author feher
 *
 */
public class CreateTableController extends AbstractPokerClientController {

	private final String SUCC_CREATE_TABLE_MSG = "A táblát sikeresen létrehoztad!";
	private final String SUCC_MODIFY_TABLE_MSG = "A táblát sikeresen modosítottad!";
	private final String ERR_TEXTFIELD_TEXT = "Hibás szám formátum a %s mezőben!";
	private final String ERR_STYLECLASS = "hiba";

	@FXML private ComboBox<String> gameTypeComboBox;

	@FXML private TextField tableNameTextField;

	@FXML private TextField maxTimeField;

	@FXML private TextField maxPlayerTextField;

	@FXML private TextField bigBlindField;

	@FXML private Button createTableButton;

	@FXML private Button backButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fillPokerTypes();
		dummyData();
		setParamPokerTable();
	}

	/**
	 * Játékstílusokkal tölti fel a legördülő listát.
	 */
	private void fillPokerTypes() {
		List<String> pokerTypes = new ArrayList<>();
		for (int i = 0; i < PokerType.values().length; i++) {
			pokerTypes.add(PokerType.values()[i].toString());
		}
		gameTypeComboBox.setItems(FXCollections.observableArrayList(pokerTypes));
	}

	@Deprecated
	private void dummyData() {
		tableNameTextField.setText("próba szerver 1");
		gameTypeComboBox.getSelectionModel().select(0);
		maxTimeField.setText("39");
		maxPlayerTextField.setText("5");
		bigBlindField.setText("12");
	}

	/**
	 * Ha a TableLister controllernél tábla módosításra kattintottam, akkor kiveszem a paramétert. (A kiválasztott táblát, amolyan ThreadContext...)
	 */
	private void setParamPokerTable() {
		PokerTable pokerTable = Model.getParamPokerTable();
		if (pokerTable != null) {
			tableNameTextField.setText(pokerTable.getName());
			gameTypeComboBox.getSelectionModel().select(pokerTable.getPokerType().getName());
			maxTimeField.setText(String.valueOf(pokerTable.getMaxTime()));
			maxPlayerTextField.setText(String.valueOf(pokerTable.getMaxPlayers()));
			bigBlindField.setText(pokerTable.getBigBlind().toString());
		}
	}

	/**
	 * A CREATE TABLE gomb click handlerje.
	 * @param event az esemény
	 */
	@FXML protected void createTableHandler(ActionEvent event) {
		maxTimeField.getStyleClass().remove(ERR_STYLECLASS);
		maxPlayerTextField.getStyleClass().remove(ERR_STYLECLASS);
		bigBlindField.getStyleClass().remove(ERR_STYLECLASS);

		String tableName = tableNameTextField.getText();

		PokerType pokerType = null;
		Integer maxTime = null;
		Integer maxPlayers = null;
		BigDecimal maxBet = null;
		BigDecimal bigBlind = null;
		try {
			pokerType = PokerType.valueOf(gameTypeComboBox.getSelectionModel().getSelectedItem());
		} catch (IllegalArgumentException ex) {
			showErrorAlert("Hibás játék mód!");
			return;
		}
		try {
			maxTime = new Integer(maxTimeField.getText());
		} catch (NumberFormatException ex) {
			maxTimeField.getStyleClass().add(ERR_STYLECLASS);
			showErrorAlert(String.format(ERR_TEXTFIELD_TEXT, "gondolkodási idő"));
			return;
		}
		try {
			maxPlayers = Integer.valueOf(maxPlayerTextField.getText());
		} catch (NumberFormatException ex) {
			maxPlayerTextField.getStyleClass().add(ERR_STYLECLASS);
			showErrorAlert(String.format(ERR_TEXTFIELD_TEXT, "maximum játékos"));
			return;
		}
		try {
			bigBlind = BigDecimal.valueOf(Double.valueOf(bigBlindField.getText()));
		} catch (NumberFormatException ex) {
			bigBlindField.getStyleClass().add(ERR_STYLECLASS);
			showErrorAlert(String.format(ERR_TEXTFIELD_TEXT, "alaptét"));
			return;
		}
		
		// nincs/nem volt átadandó paraméter, tehát új táblát kell létrehoznom
		if (Model.getParamPokerTable() == null) {
			// ekkor új táblát hozunk létre
			try {
				PokerTable t = new PokerTable(tableName, maxTime, maxPlayers, bigBlind, pokerType);
				model.createTable(t);
				showSuccessAlert(SUCC_CREATE_TABLE_MSG);
				frameController.setTableListerFXML();
			} catch (PokerDataBaseException e) {
				showErrorAlert(e.getMessage());
			} catch (RemoteException e) {
				remoteExceptionHandler();
			}
		} else {
			// különben pedig volt paraméter => módosítunk
			try {
				PokerTable t = Model.getParamPokerTable();
				t.setName(tableName);
				t.setMaxTime(maxTime);
				t.setMaxPlayers(maxPlayers);
				t.setMaxPlayers(maxPlayers);
				t.setBigBlind(bigBlind);
				t.setPokerType(pokerType);
				model.modifyTable(t);
				showSuccessAlert(SUCC_MODIFY_TABLE_MSG);
				frameController.setTableListerFXML();
			} catch (PokerDataBaseException e) {
				showErrorAlert(e.getMessage());
			} catch (RemoteException e) {
				remoteExceptionHandler();
			}
			model.setParameterPokerTable(null);
		}
	}

	/**
	 * A BACK gomb click handlerje.
	 * @param event az esemény
	 */
	@FXML protected void backHandler(ActionEvent event) {
		frameController.setTableListerFXML();
	}
}