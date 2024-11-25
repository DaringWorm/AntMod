package com.daringworm.antmod;

public class DebugHelper {
    public static int numberOfPathsRequested = 0;
    private static boolean pauseAnts = false;
    public static void setPaused(boolean bool){pauseAnts = bool;}
    public static boolean getPaused(){return pauseAnts;}
}
