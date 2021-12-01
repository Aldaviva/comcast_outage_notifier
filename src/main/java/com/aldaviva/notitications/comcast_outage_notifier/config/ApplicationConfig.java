package com.aldaviva.notitications.comcast_outage_notifier.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.CustomPlaywrightImpl;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@ComponentScan("com.aldaviva.notitications.comcast_outage_notifier")
public class ApplicationConfig {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationConfig.class);

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
		objectMapper.registerModule(new JodaModule());
		return objectMapper;
	}

	/**
	 * Configuration comes from
	 *
	 * <ol>
	 * <li><code>comcast_outage_notifier_conf/*.properties</code> on the classpath, like in Jetty's <code>conf</code> directory (highest priority)</li>
	 * <li><code>META-INF/dev/*.properties</code>, if the servlet container is launched with <code>-Denv=dev</code></li>
	 * <li><code>META-INF/test/*.properties</code>, if launched by Surefire/TestNG</li>
	 * <li><code>META-INF/prod/*.properties</code> (lowest priority)</li>
	 * </ol>
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
		final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		final List<Resource> locations = new ArrayList<>();
		final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

		try {
			locations.addAll(Arrays.asList(pathMatchingResourcePatternResolver.getResources("META-INF/prod/*.properties")));
		} catch (final FileNotFoundException e) {
		}

		final String environment = System.getProperty(SpringConfig.ENV);
		if (environment != null) {
			try {
				locations.addAll(Arrays.asList(pathMatchingResourcePatternResolver.getResources("META-INF/" + environment + "/*.properties")));
			} catch (final FileNotFoundException e) {
			}
		}

		try {
			locations.addAll(Arrays.asList(pathMatchingResourcePatternResolver.getResources("classpath:comcast_outage_notifier_conf/*.properties")));
		} catch (final FileNotFoundException e) {
		}

		locations.add(pathMatchingResourcePatternResolver.getResource("./config.properties")); //filesystem?

		propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
		propertySourcesPlaceholderConfigurer.setLocations(locations.toArray(new Resource[] {}));
		return propertySourcesPlaceholderConfigurer;
	}

	@Bean
	public Playwright playwright() {
		LOGGER.debug("Ensuring Chromium is installed");
		return CustomPlaywrightImpl.create("chromium");
	}
}
