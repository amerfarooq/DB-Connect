
public class DBClientManager {

	private static DBClient client = null;

	private DBClientManager() {}

	public static DBClient getClient() {
		if (client == null)
			client = new DBClient();

		return client;
	}

}
