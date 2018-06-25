package co.aerobotics.android.activities.interfaces;

/**
 * Created by michaelwootton on 8/3/17.
 */

public interface APIContract {
    String server = "https://sherlock.aerobotics.io/";
    // String server = "http://192.168.100.119:8080/";
    String USER_DETAILS_URL = "https://aeroview.aerobotics.co.za/aeroview-mobile/user-details/";
    String USER_SIGNUP_URL = "https://aeroview.aerobotics.co.za/aeroview-mobile/mobile-sign-up/";
    String AEROVIEW_SIGNUP = "https://aeroview.aerobotics.co.za/sign-up?src=app";
    String AEROVIEW_RESET = "https://aeroview.aerobotics.co.za/aeroview-mobile/reset-password/";
    // String GATEWAY_ORCHARDS = "https://aeroview.aerobotics.co.za/aeroview-mobile/add-boundary/";

    String USER_AUTH_GET_TOKEN = server + "gateway/auth/get_token/";
    String GATEWAY_ORCHARDS = server + "gateway/orchards/";
    String GATEWAY_FARMS = server + "gateway/farms/";
    String GATEWAY_CROPTYPES = server + "gateway/croptypes/";
    String GATEWAY_USERS = server + "gateway/users/";
    String GATEWAY_CROPFAMILIES = server + "gateway/cropfamilies/";
}
