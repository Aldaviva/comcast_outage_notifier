package com.aldaviva.notitications.comcast_outage_notifier.services.comcast;

import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.IsVisibleOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.LoadState;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutageMapClientImpl implements OutageMapClient {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OutageMapClientImpl.class);

	private static final String STATUS_MAP_URL = "https://www.xfinity.com/support/status-map";
	private static final String OUTAGE_DATA_API_URL = "https://api.sc.xfinity.com/outagedata";

	@Autowired private Playwright playwright;
	@Autowired private ObjectMapper objectMapper;

	@Value("${comcast.username}") private String username;
	@Value("${comcast.password}") private String password;

	@Override
	public OutageData fetchOutageData() {
		LOGGER.debug("Launching Chromium");

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				playwright.close();
			}

		}));

		try (final Browser browser = playwright.chromium().launch(new LaunchOptions().setHeadless(false))) {
			final BrowserContext context = browser.newContext();

			final CountDownLatch responseReceived = new CountDownLatch(1);
			final AtomicReference<byte[]> outageDataApiResponseBytes = new AtomicReference<>();
			context.onRequest(new Consumer<Request>() {
				@Override
				public void accept(final Request t) {
					if (OUTAGE_DATA_API_URL.equals(t.url())) {
						LOGGER.debug("Outgoing request to {}", t.url());
					} else {
						LOGGER.trace("Ignoring outgoing request to {}", t.url());
					}
				}
			});
			context.onRequestFailed(new Consumer<Request>() {
				@Override
				public void accept(final Request t) {
					if (OUTAGE_DATA_API_URL.equals(t.url())) {
						LOGGER.debug("Request failed for {}", t.url());
					} else {
						LOGGER.trace("Ignoring finished failed for {}", t.url());
					}
				}
			});
			context.onRequestFinished(new Consumer<Request>() {
				@Override
				public void accept(final Request t) {
					if (OUTAGE_DATA_API_URL.equals(t.url())) {
						LOGGER.debug("Request finished for {}", t.url());
					} else {
						LOGGER.trace("Ignoring finished request for {}", t.url());
					}
				}
			});
			context.onResponse(new Consumer<Response>() {
				@Override
				public void accept(final Response response) {
					if (OUTAGE_DATA_API_URL.equals(response.url())) {
						LOGGER.debug("Found response from {}", response.url());
						outageDataApiResponseBytes.set(response.body());
						responseReceived.countDown();
					} else {
						LOGGER.trace("Ignoring response from {}", response.url());
					}
				}
			});
			context.route(OUTAGE_DATA_API_URL, new Consumer<Route>() { //annoying, without this routing, the onResponse consumer above is intermittently not fired for this request
				@Override
				public void accept(final Route t) {
					LOGGER.debug("Found routed response from {}", t.request().url());
					t.resume();
				}
			});
			final Page tab = context.newPage();

			signIn(tab);

			tab.navigate(STATUS_MAP_URL);
			LOGGER.debug("Navigated to authorized status map page");

			LOGGER.debug("Waiting for outage map API response");
			tab.waitForLoadState(LoadState.NETWORKIDLE);
			responseReceived.await();
			LOGGER.debug("Received outage map API response");

			final OutageData outageData = objectMapper.readValue(outageDataApiResponseBytes.get(), OutageData.class);
			LOGGER.debug("Parsed API response containing {} outages", outageData.serviceOutages.size());

			//			signOut(tab);

			return outageData;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void signIn(final Page tab) {
		LOGGER.debug("Navigating to anonymous support page");
		tab.navigate(STATUS_MAP_URL);

		tab.click("a.xc-header--signin-link");
		LOGGER.debug("Clicked sign in link");

		tab.click("button.cancel");
		LOGGER.debug("Clicked \"I'm not on my home network\" link");

		tab.waitForLoadState();

		if (tab.isVisible(".overlay .close", new IsVisibleOptions().setTimeout(10000))) {
			tab.click(".overlay .close");
			LOGGER.debug("Closed modal dialog");
		} else {
			tab.click("button.submit");
			LOGGER.debug("Submitted auth method choice form");
		}

		tab.fill("#user", username);
		tab.fill("#passwd", password);
		tab.click("#sign_in");
		LOGGER.debug("Submitted auth form");

		tab.waitForLoadState();
	}

	/**
	 * @deprecated There is no longer a sign out link on the outage map page
	 */
	@Deprecated
	private void signOut(final Page tab) {
		LOGGER.debug("Signing out");
		tab.click("a.xc-header--dropdown-link.xc-header--signin-signout-link"); //Sign out
		tab.waitForLoadState();
		LOGGER.debug("Signed out");
	}
}
