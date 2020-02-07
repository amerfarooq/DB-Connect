import java.sql.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class DBClient {

	private Connection connection;
    private String username;
    private String password;
    private String tableName;
    private String url;
    private String catalog;


    /*
        pass tableName as argument if user wants to change table
    */
    public void setTable(String tableName) {
        this.tableName = tableName;
    }

    /*
        pass catalogName as argument if user wants to change to a different catalog
    */
    public void setCatalog(String catalog) throws SQLException {
        this.catalog = catalog;
        connection.setCatalog(catalog);
    }

    /*
        if user wants to insert a new record into table, send arguments of all columns in comma separated form
    */
    public void insertRow(String args) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("INSERT INTO " + tableName + " VALUES (" + args + ");");
    }

    /*
        Reference: https://stackoverflow.com/questions/40841078/how-to-get-primary-keys-for-all-tables-in-jdbc
        private helper function called in update attribute
    */

    private String getPKValues(int rowNum) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String str = " WHERE ";

        ResultSet primaryKeys = meta.getPrimaryKeys(catalog, null, tableName);
        primaryKeys.next();
        String colName = primaryKeys.getString("COLUMN_NAME");
        System.out.println("key:" + colName);
        str += colName + " = '" + getAttributeValue(rowNum, colName) + "'";

        while (primaryKeys.next()) {
            colName = primaryKeys.getString("COLUMN_NAME");
            System.out.println("key:" + colName);
            str += " AND " + colName + "= '" + getAttributeValue(rowNum, colName) + "'";
        }

        str += ";";
        System.out.println(str);
        return str;
    }

    /*
        function returns an array list of catalogs in the current database
    */
    public ArrayList<Catalog> getCataloges() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();

        ResultSet ctlgRs = dbmd.getCatalogs();

        ArrayList<Catalog> catalogList = new ArrayList<>();

        while (ctlgRs.next()) {
            Catalog newCatalog = new Catalog(ctlgRs.getString(1));
            getTablesInCatalog(ctlgRs.getString(1), newCatalog);
            catalogList.add(newCatalog);
        }

        return catalogList;
    }

    /*
        private helper function used in update attribute
    */
    private String getAttributeValue(int rowNum, String colName) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + ";");
        rs.absolute(rowNum);
        return rs.getString(colName);
    }

    /*
        function expects row number, column number and new value for the cell the user wants to update
    */
//    public void updateAttribute(int rowNum, int colNum, String value) throws SQLException {
//        Statement stmt = connection.createStatement();
//        ResultSet rs = stmt.executeQuery("SELECT * FROM " + this.tableName + ";");
//        ResultSetMetaData rsmd = rs.getMetaData();
//        rs.absolute(rowNum);
//        String colName = rsmd.getColumnName(colNum);
//        String query = "Update " + this.catalog + "." + this.tableName + " set " + colName + " = '" + value + "' ";
//        query += getPKValues(rowNum);
//        System.out.println(query);
//        stmt.executeUpdate(query);
//    }

    /*
        function used to execute any form of query, expects that query an argument of type string
        function returns result set if it's a select query
    */
    public ResultSet executeQuery(String query) throws SQLException {
        String arr[] = query.split(" ", 2);
        String firstWord = arr[0];
        ResultSet rs = null;

        Statement stmt = connection.createStatement();

        if (firstWord.toLowerCase().equals("select")){
            rs = stmt.executeQuery(query);
        } else {
            stmt.executeUpdate(query);
        }

        return rs;
    }

    /*
        helper function called in getCatalogs
        updates obj Catalog with tables present in that catalog
    */
    private void getTablesInCatalog(String catalogName, Catalog catalog) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet rs = metadata.getTables(catalogName, catalogName, "%", null);

        while (rs.next()) {
            catalog.insertTable(rs.getString(3));
        }
        System.out.println();
    }

    /*
        function to drop table
        pre requisite: user has set table using setTable(
    */
    public void dropTable() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("DROP TABLE " + tableName + ";");
    }

    /*
        function that deletes all rows in the current table
        pre requisite: user has set table using setTable(
    */
    public void deleteAllRows() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("DELETE FROM " + tableName);
    }

    /*
        function returns meta data of current table in an arraylist of type AttributeMetaData
        pre requisite: user has set table using setTable(
    */
    public ArrayList<AttributeMetaData> getTableMetaData() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + ";");
        ResultSetMetaData rsmd = rs.getMetaData();

        int numColumns = rsmd.getColumnCount();
        ArrayList<AttributeMetaData> metaData = new ArrayList<>();
        for (int i = 1; i <= numColumns; ++i) {
            AttributeMetaData newAttribute = new AttributeMetaData(rsmd.getColumnName(i), rsmd.getColumnTypeName(i), Integer.toString(rsmd.getColumnDisplaySize(i)), Integer.toString(rsmd.isNullable(i)), Boolean.toString(rsmd.isAutoIncrement(i)));
            metaData.add(newAttribute);
        }

        return metaData;
    }

    /*
        function returns result set of the entire table
        pre requisite: user has set table using setTable(
    */
    public ResultSet getTable() throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery("SELECT * FROM " + tableName + ";");
    }

    /*
        function that displays the contents of a table on console
        pre requisite: user has set table using setTable(
    */
    public void printTable() throws SQLException {
        Statement stmt = connection.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + ";");
        ResultSetMetaData rsmd = rs.getMetaData();

        int numColumns = rsmd.getColumnCount();

        for (int i = 1; i <= numColumns; ++i) {
            System.out.print(rsmd.getColumnName(i) + "\t\t");
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i <= numColumns; ++i) {
                System.out.print(rs.getString(i) + "\t\t");
            }
            System.out.println();
        }
    }

    /*
	  Function expects the url in the form "jdbc:mysql://IP:port,username,password" and database type "mySQL"
	  if user wants to work with mySQL
	  if user wants to use MsAccess, put path in url and use "MsAccess" as database type
     */
    public void establishConnection(String url, String databaseType)
        throws ClassNotFoundException, SQLException {

    	if (databaseType.toLowerCase().equals("mysql")) {
	        Class.forName("com.mysql.cj.jdbc.Driver");

	        String str = url;
	        List<String> tokens = Arrays.asList(str.split(","));

	        if (tokens.size() < 3) {
	        	throw new IllegalArgumentException("Connection string does not contain the required parameters!");
	        }

	        this.username = tokens.get(1);
	        this.password = tokens.get(2);
	        this.url = tokens.get(0);
	        connection = DriverManager.getConnection(this.url, username, password);

	    }
    	else if (databaseType.toLowerCase().equals("msaccess")) {
	        this.username = null;
	        this.password = null;

	        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");

	        this.url = url;
	        connection = DriverManager.getConnection("jdbc:ucanaccess://" + url);
	    }
	}

    public ResultSet getTablesFromCatalog(String catalog) throws SQLException {
    	return connection.getMetaData().getTables(catalog, null, null, new String[] {"TABLE"});
    }

    public ResultSet getColumnsFromTable(String catalog, String table) throws SQLException {
    	return connection.getMetaData().getColumns(catalog, null, table, null);
    }

    public ResultSet getPrimaryKeys(String catalog, String table) throws SQLException {
    	return connection.getMetaData().getPrimaryKeys(this.catalog, null, this.tableName);
    }

    public Connection getConnection() {
    	return this.connection;
    }

    public ArrayList<String> getTableNames() throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ArrayList<String> names = new ArrayList<>();
//        ResultSet tables = metadata.getTables(null, null, "%", null);

        System.out.println("YOL1");

      ResultSet tables = metadata.getTables(null, null, null, new String[] {"TABLE"});
        while (tables.next()) {
            names.add(tables.getString("TABLE_NAME"));
        }
        System.out.println("YOL2");
        return names;
    }


    private void updateMySQL(int rowNum, int colNum, String value) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + ";");
        ResultSetMetaData rsmd = rs.getMetaData();
        rs.absolute(rowNum);
        String colName = rsmd.getColumnName(colNum);
        String query = "Update " + tableName + " set " + colName + " = '" + value + "' ";
        query += getPKValues(rowNum);
        System.out.println(query);
        stmt.executeUpdate(query);
    }

    public void updateAttribute(int rowNum, int colNum, String value) throws SQLException {
        if (DatabaseType.getDBType().equals("mysql")) {
        	System.out.println("SQL Edit");
            updateMySQL(rowNum, colNum, value);
        }
        else if (DatabaseType.getDBType().equals("msaccess")) {
        	System.out.println("Access Edit");
            updateMsAccess(rowNum, colNum, value);
        }
    }

    private void updateMsAccess(int rowNum, int colNum, String value) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + ";");
        ResultSetMetaData rsmd = rs.getMetaData();
        String colName = rsmd.getColumnName(colNum);
        String query = "Update " + tableName + " set " + colName + " = '" + value + "' ";
        query += getPKForAccess(rowNum);
        stmt.executeUpdate(query);
        System.out.println(query);
    }

    private String getPKForAccess(int rowNum) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String str = " WHERE ";

        ResultSet primaryKeys = meta.getPrimaryKeys(catalog, null, tableName);
        primaryKeys.next();
        String colName = primaryKeys.getString("COLUMN_NAME");
        System.out.println("key: " + colName);
        str += colName + " = '" + getAttributeValueAccess(rowNum, colName) + "'";
        return str + ";";
    }

    private String getAttributeValueAccess(int rowNum, String colName) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT + " + colName + " FROM " + tableName + ";");
        String str = "";
        int i = 1;
        while (rs.next()) {
            if (i == rowNum) {
                str = rs.getString(colName);
                break;
            }
            ++i;
        }
        return str;
    }



}