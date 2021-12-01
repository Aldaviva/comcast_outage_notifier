package com.aldaviva.notitications.comcast_outage_notifier.services.comcast;

import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData.OutageSummary;

public interface OutageService {

	OutageSummary getOutageSummary();

}