package com.o3dr.android.client.utils;

/**
 * Created by chavi on 1/27/16.
 */
public enum TxPowerComplianceCountries {
    AU("Australia"),
    FR("European Union"),
    JP("Japan"),
    US("United States");

    private String prettyName;
    TxPowerComplianceCountries(String prettyName) {
        this.prettyName = prettyName;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public static TxPowerComplianceCountries getDefaultCountry() {
        return US;
    }

    public static TxPowerComplianceCountries getDefaultEUCountry() {
        return FR;
    }
}
