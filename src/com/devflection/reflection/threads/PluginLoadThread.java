package com.devflection.reflection.threads;

import com.devflection.reflection.PluginLoader;

public class PluginLoadThread extends Thread {

    private final PluginLoader pluginLoader;
    private final int sleepTime;

    public PluginLoadThread(PluginLoader pluginLoader, int sleepTime) {
        this.pluginLoader = pluginLoader;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        while(true) {
            pluginLoader.loadPluginInstances();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
