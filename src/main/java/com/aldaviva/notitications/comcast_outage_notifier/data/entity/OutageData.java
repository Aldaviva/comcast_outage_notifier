package com.aldaviva.notitications.comcast_outage_notifier.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

public class OutageData {

	public String accountNumber;
	public ServiceAddress serviceAddress;
	public List<ServiceOutage> serviceOutages = new ArrayList<>();

	public static final class ServiceAddress {
		public Address address;
		public Location location;
		public String type;
	}

	public static final class Address {
		@JsonProperty("address") public String streetAddress;
		public String city;
		public String poBox;
		public String state;
		public String zipCode;
	}

	public static final class Location {
		@JsonProperty("lat") public double latitude;
		@JsonProperty("lng") public double longitude;
	}

	public static final class ServiceOutage {
		public OutageLocation location;
		public String zip;
		public OutageSummary summary;
	}

	public static final class OutageLocation {
		public Location marker;
		public double[][][][] border;
	}

	public static final class OutageSummary {
		public OutageType outageType;
		public int accountCount;
		@JsonProperty("etr") public DateTime estimatedTimeOfRecovery;
		public Set<ServiceName> serviceName = new HashSet<>();
		public DateTime lastUpdated;

		@Override
		public String toString() {
			return String.format("OutageSummary [outageType=%s, accountCount=%s, estimatedTimeOfRecovery=%s, serviceName=%s, lastUpdated=%s]", outageType,
			    accountCount, estimatedTimeOfRecovery, serviceName, lastUpdated);
		}
	}

	public enum ServiceName {
		TV,
		INTERNET,
		VOICE;
	}

	public enum OutageType {
		NONE,
		PLANNED,
		UNPLANNED;
	}
}
