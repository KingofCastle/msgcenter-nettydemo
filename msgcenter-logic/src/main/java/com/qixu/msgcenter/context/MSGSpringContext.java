package com.qixu.msgcenter.context;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MSGSpringContext implements ServletContextListener {
	private static WebApplicationContext springContext;

	public MSGSpringContext() {
		super();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	public static ApplicationContext getApplicationContext() {
		return springContext;
	}
}
