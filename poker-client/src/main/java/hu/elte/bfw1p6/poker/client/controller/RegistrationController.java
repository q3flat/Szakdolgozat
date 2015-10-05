package hu.elte.bfw1p6.poker.client.controller;

import java.net.URL;
import java.util.ResourceBundle;

import hu.elte.bfw1p6.poker.client.controller.main.FrameController;
import hu.elte.bfw1p6.poker.client.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class RegistrationController implements Initializable, PokerController {
	
	@FXML
	private AnchorPane rootPane;
	
	@FXML
	private Label pokerLabel;
	
	@FXML
	private Label registrationLabel;

	@FXML
	private TextField usernameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private PasswordField rePasswordField;
	
	@FXML
	private Label usernameLabel;
	
	@FXML
	private Label passwordLabel;
	
	@FXML
	private Label rePasswordLabel;
	
	@FXML
	private Button regButton;
	
	@FXML
	private Button backButton;
	
	private Model model;
	
	private FrameController frameController;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		model = new Model();
	}
	
	@FXML protected void handleRegistrationButton(ActionEvent event) {
		if (model.registration(usernameField.getText(), passwordField.getText())) {
			System.out.println("A regisztráció sikeres!");
		}
		//getClass().getClassLoader().getResource("/fxml/Game.fxml")
//		rootPane.getChildren().setAll(FXMLLoader.load(getClass().getClassLoader().getResource("anyad")));
	}
	
	@FXML protected void goToLogin(ActionEvent event) {
		frameController.setLoginFXML();
	}
	
	public void setDelegateController(FrameController fc) {
		this.frameController = fc;
	}

}
