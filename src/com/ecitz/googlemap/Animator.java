package com.ecitz.googlemap;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Animator implements Runnable {

	private static final int ANIMATE_SPEEED = 700;
	private static final int ANIMATE_SPEEED_TURN = 600;
	private static final int BEARING_OFFSET = 20;
	private final Interpolator interpolator = new LinearInterpolator();

	private boolean animating = false;

	private List<LatLng> latLngs = new ArrayList<LatLng>();

	int currentIndex = 0;

	float tilt = 90;
	float zoom = 15.5f;
	boolean upward = true;
	private Handler mHandler;
	long start = SystemClock.uptimeMillis();
	private Animator animator;
	LatLng endLatLng = null;
	LatLng beginLatLng = null;
	private List<Marker> markers;
	boolean showPolyline = false;
	private GoogleMap googleMap;
	private Marker trackingMarker;

	public Animator(GoogleMap googleMap, List<Marker> list, Handler handler) {
		this.mHandler = handler;
		this.googleMap = googleMap;
		this.markers = list;

	}

	public void setAnimator(Animator animator) {

		this.animator = animator;
	}

	public void reset() {
		resetMarkers();
		start = SystemClock.uptimeMillis();
		currentIndex = 0;
		endLatLng = getEndLatLng();
		beginLatLng = getBeginLatLng();

	}

	private void resetMarkers() {
		for (Marker marker : this.markers) {
			marker.setIcon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		}
	}

	public void stopAnimation() {
		animating = false;
		mHandler.removeCallbacks(animator);

	}

	/**
	 * Highlight the marker by index.
	 */
	private void highLightMarker(int index) {
		if (markers.size() >= index + 1) {
			highLightMarker(markers.get(index));
		}
	}

	/**
	 * Highlight the marker by marker.
	 */
	private void highLightMarker(Marker marker) {
		if (marker != null) {
			
//			marker.setIcon(BitmapDescriptorFactory
//					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//			marker.showInfoWindow();
		}
	}

	public void initialize(boolean showPolyLine) {
		reset();
		this.showPolyline = showPolyLine;

		highLightMarker(0);

		if (showPolyLine) {
			polyLine = initializePolyLine();
		}

		// We first need to put the camera in the correct position for the
		// first run (we need 2 markers for this).....
		LatLng markerPos = latLngs.get(0);
		LatLng secondPos = latLngs.get(1);

		setInitialCameraPosition(markerPos, secondPos);

	}

	private void setInitialCameraPosition(LatLng markerPos, LatLng secondPos) {

		float bearing = GoogleMapUtil.bearingBetweenLatLngs(markerPos,
				secondPos);

		trackingMarker = googleMap.addMarker(new MarkerOptions()
		.position(markerPos).title("title").snippet("snippet").icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_arrow)));

		float mapZoom = googleMap.getCameraPosition().zoom >= 16 ? googleMap
				.getCameraPosition().zoom : 16;

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(markerPos).bearing(bearing + BEARING_OFFSET).tilt(90)
				.zoom(mapZoom).build();

		googleMap.animateCamera(
				CameraUpdateFactory.newCameraPosition(cameraPosition),
				ANIMATE_SPEEED_TURN, new CancelableCallback() {

					@Override
					public void onFinish() {
						System.out.println("finished camera");
						animator.reset();
						Handler handler = new Handler();
						handler.post(animator);
					}

					@Override
					public void onCancel() {
						System.out.println("cancelling camera");
					}
				});
	}

	private Polyline polyLine;
	private PolylineOptions rectOptions = new PolylineOptions();

	private Polyline initializePolyLine() {
		// polyLinePoints = new ArrayList<LatLng>();
		rectOptions.add(latLngs.get(0));
		return googleMap.addPolyline(rectOptions);
	}

	/**
	 * Add the marker to the polyline.
	 */
	private void updatePolyLine(LatLng latLng) {
		List<LatLng> points = polyLine.getPoints();
		points.add(latLng);
		polyLine.setPoints(points);
	}

	public void startAnimation(boolean showPolyLine, List<LatLng> latLngs) {
		if (trackingMarker != null) {
			trackingMarker.remove();
		}
		this.animating = true;
		this.latLngs = latLngs;
		if (latLngs.size() > 2) {
			initialize(showPolyLine);
		}

	}

	public boolean isAnimating() {
		return this.animating;
	}

	@Override
	public void run() {

		long elapsed = SystemClock.uptimeMillis() - start;
		double t = interpolator.getInterpolation((float) elapsed
				/ ANIMATE_SPEEED);
		LatLng intermediatePosition = SphericalUtil.interpolate(beginLatLng,
				endLatLng, t);

		Double mapZoomDouble = 18.5 - (Math.abs((0.5 - t)) * 5);
		float mapZoom = mapZoomDouble.floatValue();

		System.out.println("mapZoom = " + mapZoom);

		trackingMarker.setPosition(intermediatePosition);

		if (showPolyline) {
			updatePolyLine(intermediatePosition);
		}

		if (t < 1) {
			mHandler.postDelayed(this, 16);
		} else {

			System.out.println("Move to next marker.... current = "
					+ currentIndex + " and size = " + latLngs.size());
			// imagine 5 elements - 0|1|2|3|4 currentindex must be smaller
			// than 4
			if (currentIndex < latLngs.size() - 2) {

				currentIndex++;

				endLatLng = getEndLatLng();
				beginLatLng = getBeginLatLng();

				start = SystemClock.uptimeMillis();

				Double heading = SphericalUtil.computeHeading(beginLatLng,
						endLatLng);

				highLightMarker(currentIndex);

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(endLatLng).bearing(heading.floatValue() /*
																		 * +
																		 * BEARING_OFFSET
																		 */)
						// .bearing(bearingL + BEARING_OFFSET)
						.tilt(tilt).zoom(googleMap.getCameraPosition().zoom)
						.build();

				googleMap.animateCamera(
						CameraUpdateFactory.newCameraPosition(cameraPosition),
						ANIMATE_SPEEED_TURN, null);

				// start = SystemClock.uptimeMillis();
				mHandler.postDelayed(this, 16);

			} else {
				currentIndex++;
				highLightMarker(currentIndex);
				stopAnimation();

			}

		}
	}

	private LatLng getEndLatLng() {
		return latLngs.get(currentIndex + 1);
	}

	private LatLng getBeginLatLng() {
		return latLngs.get(currentIndex);
	}

}
