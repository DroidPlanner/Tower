package org.droidplanner.android.utils.analytics;

/**
 * Components related to google analytics logic.
 */
public class GAUtils {

    //Not instantiable
    private GAUtils(){}

    /**
     * List the analytics categories used in the app.
     */
    public static enum Category {
        /**
         * Category for analytics data related to the details panel on the flight data screen.
         */
        FLIGHT_DATA_DETAILS_PANEL,

        /**
         * Category for analytics data related to the action buttons on the flight data screen.
         */
        FLIGHT_DATA_ACTION_BUTTON,

        /**
         * Category for analytics related to mavlink connection events.
         */
        MAVLINK_CONNECTION;

        @Override
        public String toString(){
            return name().toLowerCase();
        }
    }

    /**
     * List the custom dimension used in the app.
     */
    public static enum CustomDimension {
        /**
         * Custom dimension used to report the used mavlink connection type.
         */
        MAVLINK_CONNECTION_TYPE(1)
        ;

        /**
         * Custom dimension index.
         */
        private int mIndex;

        private CustomDimension(int dimenIndex){
            mIndex = dimenIndex;
        }

        /**
         * @return the custom dimension index.
         */
        public int getIndex(){
            return mIndex;
        }
    }
}
