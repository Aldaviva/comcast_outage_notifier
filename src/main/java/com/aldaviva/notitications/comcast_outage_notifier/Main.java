package com.aldaviva.notitications.comcast_outage_notifier;

import com.aldaviva.notitications.comcast_outage_notifier.config.SpringConfig;
import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData.OutageSummary;
import com.aldaviva.notitications.comcast_outage_notifier.services.comcast.OutageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) {
		final SpringConfig springConfig = new SpringConfig();
		springConfig.start();

		final int exitCode = 0;
		try {
			final OutageSummary outage = springConfig.getBean(OutageService.class).getOutageSummary();
			LOGGER.info(outage != null ? outage.toString() : "No outages");
		} finally {
			springConfig.stop();
		}

		System.exit(exitCode);
	}

}
