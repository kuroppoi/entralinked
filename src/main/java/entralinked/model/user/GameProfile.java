package entralinked.model.user;

public class GameProfile {
    
    private int id;
    private String firstName;
    private String lastName;
    private String aimName;
    private String zipCode;
    
    public GameProfile(int id) {
        this.id = id;
    }
    
    public GameProfile(int id, String firstName, String lastName, String aimName, String zipCode) {
        this(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.aimName = aimName;
        this.zipCode = zipCode;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setAimName(String aimName) {
        this.aimName = aimName;
    }
    
    public String getAimName() {
        return aimName;
    }
    
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    public String getZipCode() {
        return zipCode;
    }
}
