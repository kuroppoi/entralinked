package entralinked.model.avenue;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvenueShopType {
    
    @JsonEnumDefaultValue
    RAFFLE,
    FLORIST,
    SALON,
    ANTIQUE,
    DOJO,
    CAFE,
    MARKET
}
