package com.ecitz.googlemap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

public class DirectionsMapActivity extends FragmentActivity {

	private static final HttpTransport HTTP_TRANSPORT = AndroidHttp
			.newCompatibleTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final String PLACES_API_KEY = "AIzaSyCgkHuIw2NlNkxC8Ruc_ylW_esUTEo1Jqk";
	private List<LatLng> latLngs = new ArrayList<LatLng>();
	private MapFragment mMapFragment;
	private GoogleMap googleMap;
	private List<Marker> markers = new ArrayList<Marker>();
	private Animator animator;
	private final Handler mHandler = new Handler();
	private Handler handler = new Handler();
	private Random random = new Random();
	private Runnable runner = new Runnable() {
		@Override
		public void run() {
			// setHasOptionsMenu(true);
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);

		setContentView(R.layout.layout_directions);
		this.handler.postDelayed(this.runner, this.random.nextInt(2000));
		this.mMapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);
		this.googleMap = this.mMapFragment.getMap();
		this.googleMap.getUiSettings().setZoomControlsEnabled(false);
		this.googleMap.getUiSettings().setCompassEnabled(false);
		this.googleMap.setMyLocationEnabled(true);
		Intent localIntent = getIntent();
		String from = localIntent.getExtras().getString("from");
		String to = localIntent.getExtras().getString("to");
		String mode = localIntent.getExtras().getString("mode");
		
		this.animator = new Animator(this.googleMap, this.markers, this.mHandler);
		this.animator.setAnimator(animator);
		new DirectionsFetcher(from, to, mode).execute(new URL[0]);
		
		
		Log.i("", "googleMap:"+(googleMap==null)+ "animator:"+(animator==null)+" "+mode +" "+to +" "+from);
	}

	/**
	 * Adds a list of markers to the map.
	 */
	private void addPolylineToMap(List<LatLng> latLngs) {
		PolylineOptions options = new PolylineOptions();
		for (LatLng latLng : latLngs) {
			options.add(latLng);
			options.color(R.color.map_line);
		}
		googleMap.addPolyline(options);
	}
	
	

	/**
	 * Clears all markers from the map.
	 */
	public void clearMarkers() {
		googleMap.clear();
		markers.clear();
	}

	@SuppressLint("NewApi")
	private class DirectionsFetcher extends AsyncTask<URL, Integer, Void> {

		private String origin;
		private String destination;
		private String mode;

		public DirectionsFetcher(String origin, String destination, String mode) {
			this.origin = origin;
			this.destination = destination;
			this.mode = mode;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			clearMarkers();

		}

		protected Void doInBackground(URL... urls) {
			try {
				HttpRequestFactory requestFactory = HTTP_TRANSPORT
						.createRequestFactory(new HttpRequestInitializer() {
							@Override
							public void initialize(HttpRequest request) {

								request.setParser(new JsonObjectParser(
										JSON_FACTORY));
							}
						});

				GenericUrl url = new GenericUrl(
						"http://maps.googleapis.com/maps/api/directions/json");
				url.put("origin", origin);
				url.put("destination", destination);
				url.put("sensor", false);
				url.put("mode", mode);
				if (this.mode.equals("transit")) {
					url.put("departure_time", Integer.valueOf(1343605500));
				}

				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				DirectionsResult directionsResult = httpResponse
						.parseAs(DirectionsResult.class);

				String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
				latLngs = GoogleMapUtil.decode(encodedPoints);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;

		}

		protected void onProgressUpdate(Integer... progress) {
		}

		protected void onPostExecute(Void result) {
			System.out.println("Adding polyline: "+latLngs.size()+" size");
			addPolylineToMap(latLngs);
			System.out.println("Fix Zoom");
			GoogleMapUtil.fixZoomForLatLngs(googleMap, latLngs);
			System.out.println("Start anim");
			animator.startAnimation(false, latLngs);
		}
	}

	private void resetMarkers() {
		for (Marker marker : this.markers) {
			marker.setIcon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		}
	}

	public static class DirectionsResult {

		@Key("routes")
		public List<Route> routes;

	}

	public static class Route {
		@Key("overview_polyline")
		public OverviewPolyLine overviewPolyLine;

	}

	public static class OverviewPolyLine {
		@Key("points")
		public String points;

	}

}
