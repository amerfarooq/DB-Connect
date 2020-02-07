import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import com.github.daytron.simpledialogfx.data.HeaderColorStyle;
import com.github.daytron.simpledialogfx.dialog.Dialog;
import com.github.daytron.simpledialogfx.dialog.DialogType;
import com.jfoenix.controls.JFXCheckBox;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class TableViewController implements Initializable {

	protected ResultSet table;
	protected String catalogName;
	protected String tableName;

	@FXML
    protected TableView tableView = new TableView<>();

	@FXML
    private JFXCheckBox editableChkBox;

	@FXML
	private Text connText;

	@FXML
    private ImageView refresh;

    public TableViewController(ResultSet rs, String catalog, String table) {
		this.table = rs;
		this.catalogName = catalog;
		this.tableName = table;
    }

    public TableViewController(ResultSet rs) {
		this.table = rs;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
			if (DatabaseType.getDBType() == "mysql")
				connText.setText("MySQL 8.0 Connection active");
			else
				connText.setText("MSAccess Connection active");

			addColumns();
			addRows();
		}
		catch (SQLException e) {
			System.err.println("Error in TableViewController!");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void addColumns() throws SQLException {

		for (int i = 0; i < table.getMetaData().getColumnCount(); i++) {
			final int j = i;
		    TableColumn column = new TableColumn<>();
		    column.setText(table.getMetaData().getColumnName(i + 1));

		    column.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>(){
		        public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
		            return new SimpleStringProperty(param.getValue().get(j).toString());
		        }
		    });
		    column.setCellFactory(TextFieldTableCell.<String> forTableColumn());

		    column.setOnEditCommit(new EventHandler<CellEditEvent<ObservableList, String>>() {

				@Override
				public void handle(CellEditEvent<ObservableList, String> event) {

					System.out.println("Database and Table: " + catalogName + " " + tableName);

					System.out.println("New cell value: " + event.getNewValue());
					int row = event.getTablePosition().getRow();
					int col = event.getTablePosition().getColumn();

					System.out.println("Row: " + row);
					System.out.println("Col: " + col);

					ObservableList<String> list = event.getTableView().getItems().get(row);

					try {
						if (DatabaseType.getDBType() == "mysql")
							DBClientManager.getClient().setCatalog(catalogName);

						DBClientManager.getClient().setTable(tableName);
						DBClientManager.getClient().updateAttribute(row + 1, col + 1, event.getNewValue());
					}
					catch (SQLException e) {
						Dialog dialog = new Dialog(DialogType.ERROR, "Error syncing with database", e.getMessage());
						dialog.setTitle("Data Error");
		    			dialog.setHeaderColorStyle(HeaderColorStyle.GLOSS_POMEGRANATE);
						dialog.showAndWait();

						tableView.refresh();
						e.printStackTrace();
					}
				}
			});

		    tableView.getColumns().addAll(column);
		}
	}

	protected void addRows() throws SQLException {

		while (table.next()) {
			ObservableList<String> row = FXCollections.observableArrayList();
			for (int i = 0; i < table.getMetaData().getColumnCount(); i++) {
				String colVal;

				// https://stackoverflow.com/questions/25373126/workaround-in-ucanaccess-for-multi-value-fields-incompatible-data-type-in-conv
				try {
					colVal = table.getString(i + 1);
				}
				catch (Exception e){
					colVal = table.getObject(i + 1).toString();
				}

				if (table.wasNull())
					colVal = "null";

				row.addAll(colVal);
			}
			tableView.getItems().add(row);
		}
	}

	@FXML
    void checkBoxSelected(ActionEvent event) {
		if (editableChkBox.isSelected()) {
			this.tableView.setEditable(true);
		}
		else {
			this.tableView.setEditable(false);
		}
    }

    @FXML
    private void refreshClicked(MouseEvent event) {
    	this.tableView.refresh();
    }
}

