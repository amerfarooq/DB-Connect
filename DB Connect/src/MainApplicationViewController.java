import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTabPane;
import com.sun.javafx.iio.ios.IosImageLoaderFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;


public class MainApplicationViewController implements Initializable {

    @FXML
    private TreeTableView<String> dbNavigatorView;

    @FXML
    private TreeTableColumn<String, String> dbNavigatorColumn;

    @FXML
    private JFXTabPane tabPane;

    private String dbType;
    private String db;
    private String table;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (DatabaseType.getDBType() == "mysql") {
			initializeForMySQL();
		}
		if (DatabaseType.getDBType() == "msaccess") {
			initializeForMSAccess();
		}
		else {
			System.err.println("No DB selected");
		}
	}

	private void initializeForMySQL() {

		ArrayList<Catalog> catalogs = null;
		try {
			catalogs = DBClientManager.getClient().getCataloges();
		}
		catch (SQLException e) {
			System.err.println("Catalogs could not be retrieved!");
			e.printStackTrace();
		}

		TreeItem<String> root = new TreeItem<>("Catalogs");

		for (Catalog cat : catalogs) {
			TreeItem<String> catalog = new TreeItem<>(cat.getCatalogName());

			ArrayList<String> tableNames = cat.getTables();
			for (String table : tableNames) {
				catalog.getChildren().add(new TreeItem<String>(table));
			}
			root.getChildren().add(catalog);
		}
	    root.setExpanded(true);

		dbNavigatorColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<String, String> param) {
				return new SimpleStringProperty(param.getValue().getValue());
			}
		});

		dbNavigatorView.setRoot(root);
		dbNavigatorView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> navigationHandlerMySQL(newValue));
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> tabChangeHandlerMySQL(nv));
	}

	private void initializeForMSAccess() {
		System.out.println("MSaccess initialzing");

		ArrayList<String> tableNames = null;
		try {
			tableNames = DBClientManager.getClient().getTableNames();
		}
		catch(SQLException e) {
			System.err.println("MSAccess Tables could not be retrieved!");
			e.printStackTrace();
			System.exit(0);
		}

		TreeItem<String> root = new TreeItem<>("Tables");
		for (String tb : tableNames) {
			root.getChildren().add(new TreeItem<String>(tb));
		}
		root.setExpanded(true);

		dbNavigatorColumn.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<String, String> param) {
				return new SimpleStringProperty(param.getValue().getValue());
			}
		});
		dbNavigatorView.setRoot(root);
		dbNavigatorView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> navigationHandlerAccess(newValue));
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> tabChangeHandlerAccess(nv));
	}

	public void setDBType(String db) {
		this.dbType = db;
	}

	private void navigationHandlerMySQL(Object newValue) {
		TreeItem item = (TreeItem) newValue;

		if (item.getParent() == null) {
			System.out.println("Catalogs selected!");
		}
		else if (item.getParent().getParent() == null ){
			System.out.println("Database: " + item.getValue().toString());
			this.db = item.getValue().toString();
			this.table = null;
			loadDatabaseTabsMySQL();
		}
		else {
			System.out.println("Table: " + item.getValue().toString());
			this.table = item.getValue().toString();
			this.db = item.getParent().getValue().toString();
			loadTableTabs();
		}
	}

	private void navigationHandlerAccess(Object newValue) {
		TreeItem item = (TreeItem) newValue;

		if (item.getParent() == null) {
			System.out.println("Database: " + item.getValue().toString());
			this.db = item.getValue().toString();
			this.table = null;
			loadDatabaseTabsAccess();
		}
		else {
			System.out.println("Table: " + item.getValue().toString());
			this.table = item.getValue().toString();
			this.db = item.getParent().getValue().toString();
			loadTableTabs();
		}
	}

	private void tabChangeHandlerMySQL(Object newValue) {
		if (newValue == null) return;

		Tab selectedTab = (Tab) newValue;
		FXMLLoader loader = null;

		if (selectedTab.getText() == "SQL") {
			loader = new FXMLLoader(getClass().getResource("SQLView.fxml"));
			SQLViewController cont = new SQLViewController();
			loader.setController(cont);
		}
		else {
			loader = new FXMLLoader(getClass().getResource("TableView.fxml"));

			try {

				DBClient client = DBClientManager.getClient();
				client.setCatalog(this.db);
				ResultSet rset = null;
				TableViewController cont = null;

				// Display tables in a database
				if (this.table == null) {
					rset = client.getTablesFromCatalog(this.db);
					cont = new TableViewController(rset, this.db, this.table);
				}
				else {
					// Display a table's content
					if (selectedTab.getText() == "Content") {
						rset = client.executeQuery("select * from " + this.table);
						cont = new TableViewController(rset, this.db, this.table);
					}
					// Display information on columns of table
					else {
						rset = client.getColumnsFromTable(this.db, this.table);
						cont = new TableColumnViewController(rset, this.db, this.table);
					}
				}

				loader.setController(cont);
			}
			catch (SQLException e) {
				System.out.println("SQLError in Main controller!");
				e.printStackTrace();
			}
		}

		try {
			selectedTab.setContent(loader.load());
		}
		catch(IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("FXML file for tab not found!");
		}
	}

	private void tabChangeHandlerAccess(Object newValue) {
		if (newValue == null) return;

		Tab selectedTab = (Tab) newValue;
		FXMLLoader loader = null;

		if (selectedTab.getText() == "SQL") {
			loader = new FXMLLoader(getClass().getResource("SQLView.fxml"));
			SQLViewController cont = new SQLViewController();
			loader.setController(cont);
		}
		else {
			loader = new FXMLLoader(getClass().getResource("TableView.fxml"));

			try {
				DBClient client = DBClientManager.getClient();
				client.setTable(this.table);
				ResultSet rset = null;
				TableViewController cont = null;

				// Display a table's content
				if (selectedTab.getText() == "Content") {
					rset = client.getTable();
					cont = new TableViewController(rset, this.db, this.table);
				}
				// Display information on columns of table
				else {
					rset = client.getColumnsFromTable(null, this.table);
					cont = new TableColumnViewController(rset, this.db, this.table);
				}


			loader.setController(cont);
			}
			catch (SQLException e) {
				System.out.println("SQLError in Main controller!");
				e.printStackTrace();
			}
		}

		try {
			selectedTab.setContent(loader.load());
		}
		catch(IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.err.println("FXML file for tab not found!");
		}
	}

	private void loadDatabaseTabsMySQL() {
		Tab tables = new Tab("Tables");
		Tab sql = new Tab("SQL");

		tabPane.getTabs().clear();
		tabPane.getTabs().add(tables);
		tabPane.getTabs().add(sql);
	}

	private void loadDatabaseTabsAccess() {
		Tab sql = new Tab("SQL");
		tabPane.getTabs().clear();
		tabPane.getTabs().add(sql);
	}

	private void loadTableTabs() {
		Tab content = new Tab("Content");
		Tab cols = new Tab("Columns");
		Tab sql = new Tab("SQL");

		tabPane.getTabs().clear();
		tabPane.getTabs().add(content);
		tabPane.getTabs().add(cols);
		tabPane.getTabs().add(sql);
	}
}
