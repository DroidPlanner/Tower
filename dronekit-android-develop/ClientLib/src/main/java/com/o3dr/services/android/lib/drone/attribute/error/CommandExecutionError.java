package com.o3dr.services.android.lib.drone.attribute.error;

/**
 * List the possible command execution errors.
 * Created by Fredia Huya-Kouadio on 6/24/15.
 */
public class CommandExecutionError {

    /**
     * Command execution was temporarily rejected. You may try again at a later time.
     */
    public static final int COMMAND_TEMPORARILY_REJECTED = 1;

    /**
     * Command execution was denied.
     */
    public static final int COMMAND_DENIED = 2;

    /**
     * Command is not supported by the target autopilot.
     */
    public static final int COMMAND_UNSUPPORTED = 3;

    /**
     * Command execution failed.
     */
    public static final int COMMAND_FAILED = 4;

    //Prevent object instantiation.
    private CommandExecutionError(){}
}
