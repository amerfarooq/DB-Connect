
public class DatabaseType {

	private static String dbType = null;

	public static void setDBType(String db) {
		if (dbType == null) dbType = db;
	}

	public static String getDBType() {
		return dbType;
	}
}
