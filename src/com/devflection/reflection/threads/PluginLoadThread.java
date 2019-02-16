package com.devflection.reflection.threads;

import com.devflection.reflection.pluginLoader.IPluginLoader;

public class PluginLoadThread extends Thread {

    private final IPluginLoader pluginLoader;
    private final int sleepTime;

    public PluginLoadThread(IPluginLoader pluginLoader, int sleepTime) {
        this.pluginLoader = pluginLoader;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        while (true) {
            pluginLoader.loadPlugins();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // log information about interruption
            }
        }
    }
}
