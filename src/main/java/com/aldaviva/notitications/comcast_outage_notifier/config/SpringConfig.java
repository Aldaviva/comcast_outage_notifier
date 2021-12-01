package com.aldaviva.notitications.comcast_outage_notifier.config;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringConfig {
	public static final String ENV = "env";
	public static final String ENV_PROD = "prod";
	public static final String ENV_DEV = "dev";
	public static final String ENV_TEST = "test";
	public static final String PROFILE_TEST = "test";

	private AnnotationConfigApplicationContext context;

	public void start() {
		if (context == null) {
			initLogging();

			context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
			context.start();
		}
	}

	private void initLogging() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	public void stop() {
		if (context != null) {
			context.stop();
			context = null;
		}
	}

	public <T> T getBean(final Class<T> type) {
		return context != null ? context.getBean(type) : null;
	}

	public Object getBean(final String name) throws BeansException {
		return context != null ? context.getBean(name) : null;
	}

	public <T> T getBean(final String name, final Class<T> requiredType) throws BeansException {
		return context != null ? context.getBean(name, requiredType) : null;
	}

	public Object getBean(final String name, final Object... args) throws BeansException {
		return context != null ? context.getBean(name, args) : null;
	}

	public <T> T getBean(final Class<T> requiredType, final Object... args) throws BeansException {
		return context != null ? context.getBean(requiredType, args) : null;
	}

}