package com.devflection.reflection;

public class Plugin1 implements DevflectionPlugin {

	private Plugin1WorkerThread pluginThread;

	@Override
	public void startPlugin() {
		pluginThread = new Plugin1WorkerThread();
		pluginThread.start();
	}

	@Override
	public void stopPlugin() {
		if (pluginThread != null) {
			pluginThread.stopExecution();
		}
	}

	@Override
    public String getPluginName() {
		return "Plugin1";
	}
}
