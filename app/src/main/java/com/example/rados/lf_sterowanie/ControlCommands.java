package com.example.rados.lf_sterowanie;

public final class ControlCommands {
    static final String[] TURN_ARRAY = {"f", "g", "h", "j", "k", "l"};
    static final String[] SPEED_ARRAY = {"\\", "r", "t", "y", "u", "i", "o", "p", "[", "]"};
    static final String PARAMETERS_QUESTION = "?";
    static final String FORWARD = "w";
    static final String REVERSE = "s";
    static final String TURNING_LEFT = "q";
    static final String TURNING_RIGHT = "e";
    static final String TURNING_LEFT_R = "a";
    static final String TURNING_RIGHT_R = "d";
    static final String PARAMETERS_LOAD = ".";
    static final String SPEED_INCREASE = ",";
    static final String KD_INCREASE = "+";
    static final String KP_INCREASE = "=";
    static final String AUTO_START = "8";
    static final String STOP = "0";
    static final String WHEELS_CLEANING = "7";
    static final String PARAMETERS_SAVE = "9";
    static final String LED1 = "b";
    static final String LED2 = "n";
    static final String LED12 = "m";
    static final String SENSORS_READINGS = "3";
    static final String NOTHING = "z";

    static final int sleepTime1 = 100;
    static final int sleepTime2 =100;
}
