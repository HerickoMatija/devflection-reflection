package com.devflection.plugin3;

public interface DevflectionPlugin {

    default void startPlugin() {
        System.out.println("Starting plugin " + getPluginName() + "....");
    }

    default void stopPlugin() {
        System.out.println("Stopping plugin " + getPluginName() + "....");
    }

    String getPluginName();
}
