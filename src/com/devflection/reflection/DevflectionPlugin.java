package com.devflection.reflection;

public interface DevflectionPlugin {

    default void startPlugin() {
        System.out.println(getClass() + " :Starting plugin " + getPluginName() + "....");
    }

    default void stopPlugin() {
        System.out.println(getClass() + ": Stopping plugin " + getPluginName() + "....");
    }

    String getPluginName();
}
