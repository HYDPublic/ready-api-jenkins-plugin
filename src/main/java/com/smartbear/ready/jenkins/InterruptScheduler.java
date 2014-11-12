package com.smartbear.ready.jenkins;

import java.util.TimerTask;

class InterruptScheduler extends TimerTask {
    Thread target = null;

    public InterruptScheduler(Thread target) {
        this.target = target;
    }

    @Override
    public void run() {
        target.interrupt();
    }

}
