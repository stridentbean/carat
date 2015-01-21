package edu.berkeley.cs.amplab.carat.android.fragments;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.lists.HogsBugsAdapter;
import edu.berkeley.cs.amplab.carat.android.protocol.ClickTracking;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;
import edu.berkeley.cs.amplab.carat.android.subscreens.AppDetailsFragment;
import edu.berkeley.cs.amplab.carat.android.ui.DrawView;
import edu.berkeley.cs.amplab.carat.android.ui.LocalizedWebView;

public class BugsOrHogsFragment extends Fragment {

	public static final String IS_BUGS = "IS_BUGS";
	protected boolean isBugs = false;

	private DrawView w = null;
	private View detailPage = null;

	/**
	 * When creating Hogs or Bugs, set which one this is here:
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean bug = getArguments().getBoolean("isBugs");
		if (bug)
			isBugs = true;
		else
			isBugs = false;
		// if (savedInstanceState != null) {
		// isBugs = savedInstanceState.getBoolean(IS_BUGS);
		// } else {
		// Log.d("BugsOrHogsFragment", "savedInstanceState=null");
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Tab tab =
		// ((ActionBarActivity)getActivity()).getSupportActionBar().getSelectedTab();
		// String bugsOrHogs = tab.getTag().toString();

		// String isBugsStr = isBugs? "IS_BUGS=true" : "IS_BUGS=false";
		// Log.d("BugsOrHogs", isBugsStr);

		View root = inflater.inflate(R.layout.hogs, container, false);

		initGraphChart(root);
		initDetailView(root);

		if (savedInstanceState != null) {
			Object o = savedInstanceState.get("savedInstance");
			if (o != null) {
				BugsOrHogsFragment previous = (BugsOrHogsFragment) o;
				TextView pn = (TextView) previous.detailPage.findViewById(R.id.name);
				ImageView pi = (ImageView) previous.detailPage.findViewById(R.id.appIcon);
				TextView pp = (TextView) previous.detailPage.findViewById(R.id.benefit);

				DrawView w = previous.w;

				((TextView) detailPage.findViewById(R.id.name)).setText(pn.getText());
				((ImageView) detailPage.findViewById(R.id.appIcon)).setImageDrawable(pi.getDrawable());
				((TextView) detailPage.findViewById(R.id.benefit)).setText(pp.getText());

				String appName = w.getAppName();

				w.setParams(isBugs ? Type.BUG : Type.HOG, appName, w.getEv(), w.getEvWithout(), w.getSampleCount(),
						w.getSampleCountWithout(), w.getError(), w.getErrorWithout(), (ViewGroup) detailPage);
				// w.postInvalidate();
			}
		}

		int resourceID = isBugs ? R.string.tab_bugs : R.string.tab_hogs;
		getActivity().setTitle(getResources().getString(resourceID));
		
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	private void initGraphChart(View root) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		detailPage = inflater.inflate(R.layout.graph, null);
		w = new DrawView(getActivity());

		OnClickListener detailViewer = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
				if (p != null) {
					String uuId = p.getString(CaratApplication.getRegisteredUuid(), "UNKNOWN");
					HashMap<String, String> options = new HashMap<String, String>();
					options.put("status", getActivity().getTitle().toString());
					options.put("type", isBugs ? "Bugs" : "Hogs");
					options.put("app", ((TextView) detailPage.findViewById(R.id.name)).getText() + "");
					options.put("benefit", ((TextView) detailPage.findViewById(R.id.benefit)).getText().toString()
							.replace('\u00B1', '+'));
					ClickTracking.track(uuId, "whatnumbers", options, getActivity());
				}
			}
		};

		View moreinfo = detailPage.findViewById(R.id.jscore_info);
		moreinfo.setOnClickListener(detailViewer);

		View item = detailPage.findViewById(R.id.benefit);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.benefit);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.name);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);
		item = detailPage.findViewById(R.id.appIcon);
		item.setClickable(true);
		item.setOnClickListener(detailViewer);

		/*
		 * detailPage.setOnTouchListener(new FlipperBackListener(this, vf,
		 * baseViewIndex, false));
		 */

		final ListView lv = (ListView) root.findViewById(android.R.id.list);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				Object o = lv.getItemAtPosition(position);
				SimpleHogBug fullObject = (SimpleHogBug) o;
				// View target = findViewById(R.id.hogsGraphView);

				AppDetailsFragment fragment = AppDetailsFragment.getInstance(Type.BUG, fullObject, isBugs);
				CaratApplication.getMainActivity().replaceFragment(fragment, "AppDetailsFragment");
			}
		});
	}

	private void initDetailView(View root) {
		LocalizedWebView webview = (LocalizedWebView) root.findViewById(R.id.detailView);

		webview.loadUrl("file:///android_asset/detailinfo.html");
		/*
		 * webview.setOnTouchListener(new FlipperBackListener(this, vf, vf
		 * .indexOfChild(detailPage), false));
		 */
	}

	@Override
	public void onDetach() {
		CaratApplication.setBugs(null);
		CaratApplication.setHogs(null);
		super.onDetach();
	}

	public void refresh() {
		if (getActivity() == null)
			Log.e("BugsOrHogsFragment", "unable to get activity");
		CaratApplication app = (CaratApplication) getActivity().getApplication();
		final ListView lv = (ListView) getActivity().findViewById(android.R.id.list);
		if (isBugs)
			lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.storage.getBugReport()));
		else
			lv.setAdapter(new HogsBugsAdapter(app, CaratApplication.storage.getHogReport()));
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		if (isBugs)
			CaratApplication.setBugs(this);
		else
			CaratApplication.setHogs(this);
		refresh();
		super.onResume();
	}

	/*
	 * Needs to take place in Activity
	 */
	/*
	 * @Override public void onBackPressed() { if (vf.getDisplayedChild() !=
	 * baseViewIndex && vf.getDisplayedChild() != emptyIndex &&
	 * vf.getDisplayedChild() != emptyBugsIndex) {
	 * vf.setOutAnimation(MainActivity.outtoRight);
	 * vf.setInAnimation(MainActivity.inFromLeft);
	 * vf.setDisplayedChild(baseViewIndex); viewIndex = baseViewIndex; } else
	 * finish(); }
	 */
}