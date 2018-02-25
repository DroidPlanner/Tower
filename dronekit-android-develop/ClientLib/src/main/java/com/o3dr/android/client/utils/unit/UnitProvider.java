package com.o3dr.android.client.utils.unit;

/**
 * Created by fhuya on 1/11/15.
 */
public interface UnitProvider {

    public final static String SQUARE_SYMBOL = "\u00B2";

    String areaToString(double areaInSqMeters);

    String distanceToString(double distanceInMeters);

    String speedToString(double speedInMetersPerSeconds);

    String electricChargeToString(double chargeInmAh);
}
