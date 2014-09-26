package com.ecitz.googlemap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

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

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private static final HttpTransport HTTP_TRANSPORT = AndroidHttp
				.newCompatibleTransport();
		private static final JsonFactory JSON_FACTORY = new JacksonFactory();
		private static final String PLACES_AUTOCOMPLETE_API = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
		public static final int RESULT_CODE = 123;
		private static final String PLACES_API_KEY = "AIzaSyB1k1X5cGfk1Wma3ewD2Xg-FmFSOOnK_J4";
		private String mMode = "driving";
		private AutoCompleteTextView startPointCompleteTextView;
		private AutoCompleteTextView endPointCompleteTextView;
		private RadioGroup modeGroup;

		public PlaceholderFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onActivityCreated(savedInstanceState);

			startPointCompleteTextView
					.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
							android.R.layout.simple_dropdown_item_1line));
			endPointCompleteTextView
					.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
							android.R.layout.simple_dropdown_item_1line));

			startPointCompleteTextView
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub
							String address = arg0.getItemAtPosition(arg2)
									.toString();
							startPointCompleteTextView.setText(address);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub

						}
					});

			endPointCompleteTextView
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							// TODO Auto-generated method stub

							String address = arg0.getItemAtPosition(arg2)
									.toString();
							endPointCompleteTextView.setText(address);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub

						}
					});
			modeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup arg0, int arg1) {
					int radioButtonId = arg0.getCheckedRadioButtonId();
					RadioButton rb = (RadioButton) getView()
							.findViewById(radioButtonId);
					mMode = rb.getText().toString();
				}
			});
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			startPointCompleteTextView = (AutoCompleteTextView) rootView
					.findViewById(R.id.txtStartPoint);
			endPointCompleteTextView = (AutoCompleteTextView) rootView
					.findViewById(R.id.txtEndPoint);
			modeGroup = (RadioGroup) rootView.findViewById(R.id.groupMode);
			 rootView.findViewById(R.id.btnSearch).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub

					Intent intent = new Intent(getActivity(), DirectionsMapActivity.class);
					intent.putExtra("from", startPointCompleteTextView.getText().toString());
					intent.putExtra("to", endPointCompleteTextView.getText().toString());
					intent.putExtra("mode", mMode);
				      startActivity(intent);
				}
			});
			return rootView;
		}

		private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
				implements Filterable {
			private ArrayList<String> resultList;

			public PlacesAutoCompleteAdapter(Context context,
					int textViewResourceId) {
				super(context, textViewResourceId);
			}

			@Override
			public int getCount() {
				return resultList.size();
			}

			@Override
			public String getItem(int index) {
				return resultList.get(index);
			}

			@Override
			public Filter getFilter() {
				Filter filter = new Filter() {
					@Override
					protected FilterResults performFiltering(
							CharSequence constraint) {
						FilterResults filterResults = new FilterResults();
						if (constraint != null) {
							// Retrieve the autocomplete results.
							resultList = autocomplete(constraint.toString());

							// Assign the data to the FilterResults
							filterResults.values = resultList;
							filterResults.count = resultList.size();
						}
						return filterResults;
					}

					@Override
					protected void publishResults(CharSequence constraint,
							FilterResults results) {
						if (results != null && results.count > 0) {
							notifyDataSetChanged();
						} else {
							notifyDataSetInvalidated();
						}
					}
				};
				return filter;
			}
		}

		private ArrayList<String> autocomplete(String input) {

			ArrayList<String> resultList = new ArrayList<String>();

			try {

				HttpRequestFactory requestFactory = HTTP_TRANSPORT
						.createRequestFactory(new HttpRequestInitializer() {
							@Override
							public void initialize(HttpRequest request) {
								request.setParser(new JsonObjectParser(
										JSON_FACTORY));
							}
						});

				GenericUrl url = new GenericUrl(PLACES_AUTOCOMPLETE_API);
				url.put("input", input);
				url.put("key", PLACES_API_KEY);
				url.put("sensor", false);

				HttpRequest request = requestFactory.buildGetRequest(url);
				HttpResponse httpResponse = request.execute();
				PlacesResult directionsResult = httpResponse
						.parseAs(PlacesResult.class);

				List<Prediction> predictions = directionsResult.predictions;
				for (Prediction prediction : predictions) {
					resultList.add(prediction.description);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return resultList;
		}

		public static class PlacesResult {

			@Key("predictions")
			public List<Prediction> predictions;

		}

		public static class Prediction {
			@Key("description")
			public String description;

			@Key("id")
			public String id;

		}

	}

}
