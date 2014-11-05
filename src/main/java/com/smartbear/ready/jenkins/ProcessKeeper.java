package com.smartbear.ready.jenkins;

import java.io.PrintStream;

public class ProcessKeeper {

    private static Process process;

    public static void addProcess(Process process) throws IllegalStateException {
        if (ProcessKeeper.process != null) {
            throw new IllegalStateException();
        }
        ProcessKeeper.process = process;
    }

    public static boolean killProcess(PrintStream out) throws IllegalStateException {
        if (ProcessKeeper.process == null) {
            throw new IllegalStateException();
        }

        // this should be forwarded to the process
        System.out.println("K");
        out.println("Sent the termination signal to the process");

        try {
            Thread.sleep(5000);
            out.println("Done waiting for the process to die graciously");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            int returnValue = process.exitValue();
            out.println("Got exit value: " + returnValue);

        } catch (IllegalThreadStateException e) {
            out.println("Process had not been terminated! Destroying it!");
            process.destroy();
        }
        return true;
    }

}
