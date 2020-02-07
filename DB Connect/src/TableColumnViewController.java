import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class TableColumnViewController extends TableViewController implements Initializable {


    public TableColumnViewController(ResultSet rs, String catalog, String table) {
    	super(rs, catalog, table);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		try {
			addColumns();
			addRows();
//			tableView.setEditable(true);
		}
		catch (SQLException e) {
			System.err.println("Error in TableColumnViewController!");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	protected void addColumns() throws SQLException {

		TableColumn colName = new TableColumn<>("Column name");
		TableColumn type = new TableColumn<>("Type");
		TableColumn size = new TableColumn<>("Column size");
		TableColumn nullable = new TableColumn<>("Nullable");
		TableColumn autoinc = new TableColumn<>("Auto Increment");
		TableColumn pk = new TableColumn<>("Is Primary Key");

		setCellValueFactory(colName, 0);
		setCellValueFactory(type, 1);
		setCellValueFactory(size, 2);
		setCellValueFactory(nullable, 3);
		setCellValueFactory(autoinc, 4);
		setCellValueFactory(pk, 5);;

		tableView.getColumns().addAll(colName, type, size, nullable, autoinc, pk);
	}

	protected void addRows() throws SQLException {

		if (isResultSetEmpty(table)) return;

		Connection conn = DBClientManager.getClient().getConnection();
		ResultSet pks = conn.getMetaData().getPrimaryKeys(catalogName, null, tableName);
		pks.next();

		while (table.next()) {
			ObservableList<String> row = FXCollections.observableArrayList();

			row.addAll(table.getString("COLUMN_NAME"));
			row.addAll(table.getString("TYPE_NAME"));
			row.addAll(table.getString("COLUMN_SIZE"));
			row.addAll(table.getString("IS_NULLABLE"));
			row.addAll(table.getString("IS_AUTOINCREMENT"));

			if (!isResultSetEmpty(pks) && (table.getString("COLUMN_NAME").trim().equals(pks.getString("COLUMN_NAME").trim()))) {
				row.add("Yes");
			}
			else
				row.add("No");

			tableView.getItems().add(row);
		}
	}

	private void setCellValueFactory(TableColumn col, int index) {
		col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>(){
	        public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
	            return new SimpleStringProperty(param.getValue().get(index).toString());
	        }
	    });
	}

	public boolean isResultSetEmpty(ResultSet rs) throws SQLException {
	    return (!rs.isBeforeFirst() && rs.getRow() == 0);
	}

}

