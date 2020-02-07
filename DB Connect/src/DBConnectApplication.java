import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class DBConnectApplication extends Application {

	public static void main(String[] args)  {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("DB Connect");

	    Scene mainScene = new Scene(FXMLLoader.load(
	  	      				new URL(DBConnectApplication.class.getResource("SelectConnectionType.fxml").toExternalForm())));

//	    new JMetro(JMetro.Style.LIGHT).applyTheme(mainScene);
	    stage.setScene(mainScene);
	    stage.show();
	}

}

