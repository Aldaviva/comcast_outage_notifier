package com.aldaviva.notitications.comcast_outage_notifier.services.comcast;

import com.aldaviva.notitications.comcast_outage_notifier.data.entity.OutageData;

public interface OutageMapClient {

	OutageData fetchOutageData();

}