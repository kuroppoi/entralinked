package entralinked.model.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import entralinked.model.dlc.Dlc;

public class User {
    
    private final String id;
    private final String password; // I debated hashing it, but.. it's a 3-digit password...
    private final Map<String, GameProfile> profiles = new HashMap<>();
    private final Map<String, Dlc> dlcOverrides = new HashMap<>();
    private int profileIdOverride; // For making it easier for the user to fix error 60000
    
    public User(String id, String password) {
        this.id = id;
        this.password = password;
    }
    
    public String getFormattedId() {
        return "%s000".formatted(id).replaceAll("(.{4})(?!$)", "$1-");
    }
    
    /**
     * @return The user's id, redacted for logging.
     */
    public String getRedactedId() {
        return "%s-XXXX-XXXX-XXXX".formatted(id.substring(0, 4));
    }
    
    public String getId() {
        return id;
    }
    
    public String getPassword() {
        return password;
    }
    
    protected void addProfile(String branchCode, GameProfile profile) {
        profiles.put(branchCode, profile);
    }
    
    protected void removeProfile(String branchCode) {
        profiles.remove(branchCode);
    }
    
    public GameProfile getProfile(String branchCode) {
        return profiles.get(branchCode);
    }
    
    public Collection<GameProfile> getProfiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }
    
    protected Map<String, GameProfile> getProfileMap() {
        return Collections.unmodifiableMap(profiles);
    }
    
    public void setDlcOverride(String type, Dlc target) {
        if(target == null) {
            dlcOverrides.remove(type);
        } else {
            dlcOverrides.put(type, target);
        }
    }
    
    public void removeDlcOverride(String type) {
        dlcOverrides.remove(type);
    }
    
    public boolean hasDlcOverride(String type) {
        return dlcOverrides.containsKey(type);
    }
    
    public Dlc getDlcOverride(String type) {
        return dlcOverrides.get(type);
    }
    
    public void setProfileIdOverride(int profileIdOverride) {
        this.profileIdOverride = profileIdOverride;
    }
    
    public int getProfileIdOverride() {
        return profileIdOverride;
    }
}
