package com.devflection.reflection.threads;

import com.devflection.reflection.PluginLoader;

import java.util.Scanner;

public class UserInputThread extends Thread {

    private final PluginLoader pluginLoader;
    private final Scanner scanner;

    public UserInputThread(PluginLoader pluginLoader) {
        this.pluginLoader = pluginLoader;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        String userInput;

        // infinite loop
        while (true) {

            // get user input
            System.out.println("Write 'start', 'stop' or 'exit' to start/stop plugins or exit...");
            userInput = scanner.nextLine();

            // execute the action that the user input
            switch (userInput) {
                case "start":
                    pluginLoader.startPlugins();
                    break;
                case "stop":
                    pluginLoader.stopPlugins();
                    break;
                case "exit":
                    System.exit(0);
                    break;
            }
        }
    }
}
