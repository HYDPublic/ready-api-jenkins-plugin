package com.smartbear.ready.jenkins;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

public class ProcessKeeper {

    private static Multimap<String, Process> processes = ArrayListMultimap.create();

    public static void addProcess(String buildId, Process process) throws IllegalStateException {
        processes.put(buildId, process);
    }

    public static boolean killProcess(String buildId, PrintStream out) throws IllegalStateException {
        for (Process process : processes.get(buildId)) {
            // this should be forwarded to the process
            try {
                process.getOutputStream().write("\n".getBytes());
                out.println("Sent the termination signal to the process");
            } catch (IOException e) {
                e.printStackTrace(out);
            }

            try {
                // Set a timer to interrupt the process if it does not return within the timeout period
                // Could be done with java8 waitFor(timeout, unit), but we probably don't want to require java8 yet
                Timer timer = new Timer();
                timer.schedule(new InterruptScheduler(Thread.currentThread()), 5000);
                process.waitFor();
                out.println("Done waiting for the process to die graciously");
            } catch (InterruptedException e) {
                e.printStackTrace(out);
            }
            try {
                int returnValue = process.exitValue();
                out.println("Got exit value: " + returnValue);

            } catch (IllegalThreadStateException e) {
                out.println("Process had not been terminated! Destroying it!");
                process.destroy();
            }
        }
        processes.removeAll(buildId);
        return true;
    }

    private static class InterruptScheduler extends TimerTask {
        Thread target = null;

        public InterruptScheduler(Thread target) {
            this.target = target;
        }

        @Override
        public void run() {
            target.interrupt();
        }

    }


}
