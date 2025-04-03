package entralinked.model.avenue;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvenueShopType {
    
    @JsonEnumDefaultValue
    RAFFLE("Raffle Shop"),
    FLORIST("Flower Shop"),
    SALON("Beauty Salon"),
    ANTIQUE("Antique Shop"),
    DOJO("Dojo"),
    CAFE("Café"),
    MARKET("Market");
    
    private final String displayName;
    
    private AvenueShopType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
