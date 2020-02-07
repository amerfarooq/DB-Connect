

import java.util.ArrayList;

public class Catalog {
    private String catalogName;
    private ArrayList<String> tables;
    
    Catalog(String catalogName) {
        tables = new ArrayList<>();
        this.catalogName = catalogName;
    }
    
    public void insertTable(String tableName) {
        tables.add(tableName);
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
    public ArrayList<String> getTables() {
        return tables;
    }
    
    public void printTablesInCatalog() {
        System.out.println("\n--> " + catalogName);
        for (int i = 0; i < tables.size(); ++i) {
            System.out.println("|---> " + tables.get(i));
        }
    }
}
