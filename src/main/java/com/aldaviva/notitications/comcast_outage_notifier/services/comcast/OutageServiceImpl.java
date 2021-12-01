package com.aldaviva.notitications.comcast_outage_notifier.services.comcast;

import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData;
import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData.OutageSummary;
import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData.ServiceOutage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OutageServiceImpl implements OutageService {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OutageServiceImpl.class);

	@Autowired private OutageMapClient outageMapClient;

	@Value("${comcast.zipCode}") private String zipCode;

	@Override
	public OutageSummary getOutageSummary() {
		LOGGER.info("Fetching outages for ZIP code {}", zipCode);
		final OutageData outageData = outageMapClient.fetchOutageData();
		for (final ServiceOutage outage : outageData.serviceOutages) {
			if (zipCode.equals(outage.zip)) {
				return outage.summary;
			}
		}
		return null;
	}
}
