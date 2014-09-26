package com.ecitz.googlemap;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

public class HistoricalPoint {

	private Date date;
	private LatLng latlng;

	public Date getDate() {
		return this.date;
	}

	public LatLng getLatlng() {
		return this.latlng;
	}

	public void setDate(Date paramDate) {
		this.date = paramDate;
	}

	public void setLatlng(LatLng paramLatLng) {
		this.latlng = paramLatLng;
	}

}
