package com.devflection.reflection;

public class Plugin1WorkerThread extends Thread {

    private volatile boolean stop = false;

    @Override
    public void run() {
        while(!stop) {
            System.out.println("Plugin1 is executing some logic...");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Plugin1 is shutting down execution...");
    }

    public void stopExecution() {
        this.stop = true;
    }
}
