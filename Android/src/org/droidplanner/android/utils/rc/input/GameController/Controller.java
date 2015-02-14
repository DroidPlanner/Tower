package org.droidplanner.android.utils.rc.input.GameController;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.util.HashMap;


public class Controller implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -8343850651938301134L;
    public HashMap<Integer, BaseCommand> joystickRemap;
    public HashMap<Integer, ButtonRemap> buttonRemap;

    @SuppressLint("UseSparseArrays")
    public Controller() {
        joystickRemap = new HashMap<Integer, BaseCommand>();
        buttonRemap = new HashMap<Integer, ButtonRemap>();
    }

    public static abstract class BaseCommand implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 4851795309306143839L;
        public int Action = -1;

        public boolean isValid() {
            return Action != -1;
        }
    }

    public static class SingleAxisRemap extends BaseCommand implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 3912943935040355024L;
        public int Trigger = -1;
        public boolean isReversed = false;

        @Override
        public boolean isValid() {
            return Trigger != -1 && super.isValid();
        }
    }

    public static class DoubleAxisRemap extends BaseCommand implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -6018865813128357425L;
        public int TriggerIncrement = -1;
        public int TriggerDecrement = -1;

        @Override
        public boolean isValid() {
            return TriggerIncrement != -1
                    && TriggerDecrement != -1
                    && super.isValid();
        }
    }

    public static class ButtonRemap extends BaseCommand implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 2834977351964447707L;
        public int Trigger = -1;
        public double ActionValue = -1;

        @Override
        public boolean isValid() {
            return Trigger != -1
                    && super.isValid();
        }
    }
}
