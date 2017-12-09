package de.robotricker.electricwires;

import java.lang.Thread.UncaughtExceptionHandler;

import org.bukkit.plugin.java.JavaPlugin;

import io.sentry.Sentry;
import io.sentry.event.UserBuilder;

public class ElectricWires extends JavaPlugin {
	
	public static ElectricWires instance;

	@Override
	public void onEnable() {
		instance = this;
		
		initSentryOnCurrentThread();
	}

	public static void initSentryOnCurrentThread() {
		Sentry.init("https://6e99a13e8f654066b8cd00927079db36:3d1a8cf711444b80bda2a6dae0b2ac9e@sentry.io/256964");
		Sentry.getContext().setUser(new UserBuilder().setUsername("RoboTricker").build());
		Sentry.getContext().addTag("thread", Thread.currentThread().getName());
		Sentry.getContext().addTag("version", ElectricWires.instance.getDescription().getVersion());

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Sentry.capture(e);
			}
		});
	}
	
}
