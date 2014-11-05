package com.smartbear.ready.jenkins;

import java.io.PrintStream;

public class ProcessKiller {

    private final Thread thread;

    public ProcessKiller(final Process process, final long timeoutInMillis, final PrintStream out) {
        thread = new Thread("ready-api-process-killer") {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();
                long expiredTime = 0L;
                try {
                    do {
                        Thread.sleep(5000L);
                        expiredTime = System.currentTimeMillis() - startTime;
                        out.println("VirtRunner - time remaining: " + (timeoutInMillis - expiredTime));
                    } while (expiredTime < timeoutInMillis);
                    out.println("VirtRunner timeout! Killing Virts process.");
                    process.destroy();
                } catch (Exception e) {
                    out.println(e);
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
    }

    public void killAfterTimeout() {
        thread.start();
    }


}
