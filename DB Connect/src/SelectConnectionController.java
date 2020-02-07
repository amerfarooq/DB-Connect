import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SelectConnectionController implements Initializable {

    @FXML
    private JFXComboBox<String> connectionsCombo;

    @FXML
    private JFXButton nextBtn;

    @Override
   	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

   		ObservableList<String> data = FXCollections.observableArrayList("MySQL", "MSAccess");
   		connectionsCombo.setItems(data);

   		connectionsCombo.valueProperty().addListener(new ChangeListener<String>() {
   			@Override
   			public void changed(ObservableValue<? extends String> selected, String oldType, String newType) {
   				System.out.println("New combox box value is: " + newType);
   	        }
   	    });
   	}

    @FXML
    void nextBtnClicked(ActionEvent event) throws IOException {
    	String selectedDB = connectionsCombo.getValue();
    	Parent parent = null;

    	if (selectedDB == "MySQL") {
    		parent = FXMLLoader.load(getClass().getResource("MySQLConnectionCreation.fxml"));
    	}
    	else if (selectedDB == "MSAccess") {
    		parent = FXMLLoader.load(getClass().getResource("MSAccessConnectionCreation.fxml"));
    	}
    	else if (selectedDB == null) {
    		System.out.println("No DB selected");
    		return;
    	}

        Scene nextForm = new Scene(parent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(nextForm);
        window.show();
    }
}

