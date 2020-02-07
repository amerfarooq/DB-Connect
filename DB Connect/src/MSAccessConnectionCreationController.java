import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.github.daytron.simpledialogfx.data.HeaderColorStyle;
import com.github.daytron.simpledialogfx.dialog.Dialog;
import com.github.daytron.simpledialogfx.dialog.DialogType;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MSAccessConnectionCreationController {

    @FXML
    private JFXTextField pathField;

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXButton finishBtn;

    @FXML
    private JFXButton backBtn;

    @FXML
    private JFXPasswordField pwdField;

    @FXML
    private JFXButton browseBtn;


    @FXML
    void backBtnClicked(ActionEvent event) throws IOException {
    	Parent parent = FXMLLoader.load(getClass().getResource("SelectConnectionType.fxml"));
        Scene connectionForm = new Scene(parent);

        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(connectionForm);
        window.show();
    }

    @FXML
    void browseBtnClicked(ActionEvent event) {
    	FileChooser fc = new FileChooser();
    	File selectedFile = fc.showOpenDialog(null);

    	if (selectedFile != null) {
    		pathField.setText(selectedFile.getAbsolutePath());
    	}
    	else {
    		System.out.println("Invalid File!");
    	}
    }

    @FXML
    void finishBtnClicked(ActionEvent event) {
    	DatabaseType.setDBType("msaccess");

    	try {
			DBClientManager.getClient().establishConnection(pathField.getText(), "msaccess");
		}
    	catch (ClassNotFoundException | SQLException e) {
			Dialog dialog = new Dialog(DialogType.ERROR, "Connection Error", e.getMessage());
			dialog.setTitle("Connection failed");
			dialog.setHeaderColorStyle(HeaderColorStyle.GLOSS_POMEGRANATE);
			dialog.showAndWait();
			return;
		}

    	FXMLLoader loader = null;
    	Parent parent = null;
    	loader = new FXMLLoader(getClass().getResource("MainApplicationView.fxml"));

    	try {
			parent = loader.load();
		} catch (IOException e) {
			System.err.println("MainApplicationView.fxml could not be loaded!");
			e.printStackTrace();
			System.exit(0);
		}

    	Scene nextForm = new Scene(parent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(nextForm);
        window.show();
    }
}



