

public class AttributeMetaData {
    private String name;
    private String type;
    private String size;
    private String isNullable;
    private String isAutoIncrement;
    
    AttributeMetaData(String name, String type, String size, String isNullable, String isAutoIncrement) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.isNullable = isNullable;
        this.isAutoIncrement = isAutoIncrement;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public String getSize() {
        return size;
    }
    
    public String getIsNullable() {
        return isNullable;
    }
    
    public String getIsAutoIncrement() {
        return isAutoIncrement;
    }
    
    public void print() {
        System.out.println(name + " " + type + " " + size + " " + isNullable + " " + isAutoIncrement);
    }
}
