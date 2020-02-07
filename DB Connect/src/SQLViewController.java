import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ResourceBundle;

import com.github.daytron.simpledialogfx.data.HeaderColorStyle;
import com.github.daytron.simpledialogfx.dialog.Dialog;
import com.github.daytron.simpledialogfx.dialog.DialogType;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

public class SQLViewController implements Initializable {

    @FXML
    private TextArea query;

    @FXML
    private JFXButton executeBtn;

    @FXML
    private TableView resultView = new TableView<>();

    private static String pastQuery;

    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if (pastQuery != null)
			query.setText(pastQuery);

	}

    @FXML
    void executeBtnClicked(ActionEvent event) {
    	System.out.println("Query: " + query.getText());
    	ResultSet rset;

    	try {
			rset = DBClientManager.getClient().executeQuery(query.getText());

			resultView.getItems().clear();
	    	resultView.getColumns().clear();
	    	resultView.refresh();

			if (rset != null)
				displayResult(rset);

			pastQuery = query.getText();
		}
    	catch (SQLException e) {
    		Dialog dialog = new Dialog(DialogType.ERROR, "SQL Query Error", e.getMessage());
			dialog.setTitle("Data Error");
			dialog.setHeaderColorStyle(HeaderColorStyle.GLOSS_POMEGRANATE);
			dialog.showAndWait();
		}

    }

	public void displayResult(ResultSet rset) throws SQLException {
		addColumns(rset);
		addRows(rset);
	}

	@SuppressWarnings("unchecked")
	protected void addColumns(ResultSet rset) throws SQLException {

		for (int i = 0; i < rset.getMetaData().getColumnCount(); i++) {
			final int j = i;
		    TableColumn column = new TableColumn<>();
		    column.setText(rset.getMetaData().getColumnName(i + 1));

		    column.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>(){
		        public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
		            return new SimpleStringProperty(param.getValue().get(j).toString());
		        }
		    });
		    resultView.getColumns().addAll(column);
		}
	}

	protected void addRows(ResultSet rset) throws SQLException {

		while (rset.next()) {
			ObservableList<String> row = FXCollections.observableArrayList();
			for (int i = 0; i < rset.getMetaData().getColumnCount(); i++) {
				String colVal;

				try {
					colVal = rset.getString(i + 1);
				}
				catch (Exception e){
					colVal = rset.getObject(i + 1).toString();
				}


				if (rset.wasNull())
					colVal = "null";

				row.addAll(colVal);
			}
			resultView.getItems().add(row);
		}
	}


}
