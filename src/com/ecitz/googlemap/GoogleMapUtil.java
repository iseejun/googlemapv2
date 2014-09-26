package com.ecitz.googlemap;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class GoogleMapUtil {

	
	public static void toggleStyle(GoogleMap googleMap) {
		if (GoogleMap.MAP_TYPE_NORMAL == googleMap.getMapType()) {
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);		
		} else {
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		}
	}

	public static Location convertLatLngToLocation(LatLng latLng) {
		Location loc = new Location("someLoc");
		loc.setLatitude(latLng.latitude);
		loc.setLongitude(latLng.longitude);
		return loc;
	}

	public static float bearingBetweenLatLngs(LatLng begin,LatLng end) {
		Location beginL= convertLatLngToLocation(begin);
		Location endL= convertLatLngToLocation(end);
		return beginL.bearingTo(endL);
	}	

	public static String encodeMarkerForDirection(Marker marker) {
		return marker.getPosition().latitude + "," + marker.getPosition().longitude;
	}

	public static void fixZoomForLatLngs(GoogleMap googleMap, List<LatLng> latLngs) {
		if (latLngs!=null && latLngs.size() > 0) {
		    LatLngBounds.Builder bc = new LatLngBounds.Builder();

		    for (LatLng latLng: latLngs) {
		        bc.include(latLng);
		    }

		    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50),4000,null);
		}
	}

	public static void fixZoomForMarkers(GoogleMap googleMap, List<Marker> markers) {
		if (markers!=null && markers.size() > 0) {
		    LatLngBounds.Builder bc = new LatLngBounds.Builder();

		    for (Marker marker : markers) {
		        bc.include(marker.getPosition());
		    }

		    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50),4000,null);
		}
	}

	public static List<LatLng> getSampleLatLngs() {
		List<LatLng> latLngs = new ArrayList<LatLng>();
		latLngs.add(new LatLng(50.961813797827055,3.5168474167585373));
        latLngs.add(new LatLng(50.96085423274633,3.517405651509762));
        latLngs.add(new LatLng(50.96020550146382,3.5177918896079063));
        latLngs.add(new LatLng(50.95936754348453,3.518972061574459));
        latLngs.add(new LatLng(50.95877285446026,3.5199161991477013));
        latLngs.add(new LatLng(50.958179213755905,3.520646095275879));
        latLngs.add(new LatLng(50.95901719316589,3.5222768783569336));
        latLngs.add(new LatLng(50.95954430150347,3.523542881011963));
        latLngs.add(new LatLng(50.95873336312275,3.5244011878967285));
        latLngs.add(new LatLng(50.95955781702322,3.525688648223877));
        latLngs.add(new LatLng(50.958855004782116,3.5269761085510254));
        return latLngs;
	}
	
	  private static final double EARTH_RADIUS = 6378137;

	    private static double rad(double value) {
	        return value * Math.PI / 180.0;
	    }

	    public static double getDistance(double lngOne, double latOne, double lngTwo, double latTwo) {
	        double radLatOne = rad(latOne);
	        double radLatTwo = rad(latTwo);
	        double a = radLatOne - radLatTwo;
	        double b = rad(lngOne) - rad(lngTwo);
	        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLatOne)
	                * Math.cos(radLatTwo) * Math.pow(Math.sin(b / 2), 2)));
	        s = s * EARTH_RADIUS;
	        s = Math.round(s * 10000) / 10000;
	        return s;
	    }

	    
	    /**
	     * Decodes an encoded path string into a sequence of LatLngs.
	     */
	    public static List<LatLng> decode(final String encodedPath) {
	        int len = encodedPath.length();

	        // For speed we preallocate to an upper bound on the final length, then
	        // truncate the array before returning.
	        final List<LatLng> path = new ArrayList<LatLng>();
	        int index = 0;
	        int lat = 0;
	        int lng = 0;

	        for (int pointIndex = 0; index < len; ++pointIndex) {
	            int result = 1;
	            int shift = 0;
	            int b;
	            do {
	                b = encodedPath.charAt(index++) - 63 - 1;
	                result += b << shift;
	                shift += 5;
	            } while (b >= 0x1f);
	            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

	            result = 1;
	            shift = 0;
	            do {
	                b = encodedPath.charAt(index++) - 63 - 1;
	                result += b << shift;
	                shift += 5;
	            } while (b >= 0x1f);
	            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

	            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
	        }

	        return path;
	    }

	    /**
	     * Encodes a sequence of LatLngs into an encoded path string.
	     */
	    public static String encode(final List<LatLng> path) {
	        long lastLat = 0;
	        long lastLng = 0;

	        final StringBuffer result = new StringBuffer();

	        for (final LatLng point : path) {
	            long lat = Math.round(point.latitude * 1e5);
	            long lng = Math.round(point.longitude * 1e5);

	            long dLat = lat - lastLat;
	            long dLng = lng - lastLng;

	            encode(dLat, result);
	            encode(dLng, result);

	            lastLat = lat;
	            lastLng = lng;
	        }
	        return result.toString();
	    }

	    private static void encode(long v, StringBuffer result) {
	        v = v < 0 ? ~(v << 1) : v << 1;
	        while (v >= 0x20) {
	            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
	            v >>= 5;
	        }
	        result.append(Character.toChars((int) (v + 63)));
	    }
}
