package com.android.canbusdemo;

public class FileHelper {
    public native int open();
    public native int write(int FF, int RTR, int ID, int DLC, int[] data);
    public native int close();
    static
    {
        System.loadLibrary("File_lib");
    }
}
