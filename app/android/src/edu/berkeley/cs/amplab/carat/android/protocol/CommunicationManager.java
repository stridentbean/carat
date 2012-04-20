package edu.berkeley.cs.amplab.carat.android.protocol;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.thrift.CaratService;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.HogBugReport;
import edu.berkeley.cs.amplab.carat.thrift.Registration;
import edu.berkeley.cs.amplab.carat.thrift.Reports;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class CommunicationManager {

	// Freshness timeout. Default: one hour
	// public static final long FRESHNESS_TIMEOUT = 3600000L;
	// 5 minutes
	public static final long FRESHNESS_TIMEOUT = 300000L;

	public static final String PREFERENCE_FIRST_RUN = "carat.first.run";

	private CaratService.Client c = null;

	private CaratApplication a = null;

	private boolean register = true;
	private SharedPreferences p = null;

	public CommunicationManager(CaratApplication a) {
		this.a = a;
		p = PreferenceManager.getDefaultSharedPreferences(this.a);
		register = p.getBoolean(PREFERENCE_FIRST_RUN, true);
	}

	private void registerMe(String uuId, String os, String model)
			throws TException {
		if (uuId == null || os == null || model == null) {
			Log.e("registerMe", "Null uuId, os, or model given to registerMe!");
			System.exit(1);
			return;
		}
		Registration registration = new Registration(uuId);
		registration.setPlatformId(model);
		registration.setSystemVersion(os);
		registration.setTimestamp(System.currentTimeMillis() / 1000.0);
		c.registerMe(registration);
	}

	public void uploadSample(Sample sample) throws TException {
		registerOnFirstRun();
		if (c == null) {
			c = ProtocolClient.getInstance(a.getApplicationContext());
		}
		if (c == null) {
			Log.e("uploadSample", "We are disconnected, not uploading.");
			return;
		}
		c.uploadSample(sample);
		ProtocolClient.close();
	}

	public void registerOnFirstRun() {
		if (register) {
			String uuId = SamplingLibrary.getUuid(a.getApplicationContext());
			String os = SamplingLibrary.getOsVersion();
			String model = SamplingLibrary.getModel();
			Log.i("CommunicationManager",
					"First run, registering this device: " + uuId + ", " + os
							+ ", " + model);
			try {
				if (c == null) {
					c = ProtocolClient.getInstance(a.getApplicationContext());
				}
				if (c == null) {
					Log.e("register",
							"We are disconnected, not registering.");
					return;
				}
				registerMe(uuId, os, model);
				p.edit().putBoolean(PREFERENCE_FIRST_RUN, false).commit();
			} catch (TException e) {
				Log.e("CommunicationManager",
						"Registration failed, will try again next time: " + e);
				e.printStackTrace();
			}
		}
	}

	public void refreshReports() {
		if (System.currentTimeMillis() - a.s.getFreshness() < FRESHNESS_TIMEOUT)
			return;
		registerOnFirstRun();
		// FIXME: Fake data for now
		String uuId = "2DEC05A1-C2DF-4D57-BB0F-BA29B02E4ABE";
		String model = "iPhone 3GS";
		String OS = "5.0.1";

		try {
			c = ProtocolClient.getInstance(a.getApplicationContext());
			if (c == null) {
				Log.e("refreshReports", "We are disconnected, not refreshing.");
				return;
			}
			refreshMainReports(uuId, OS, model);
			refreshBugReports(uuId, model);
			refreshHogReports(uuId, model);
			ProtocolClient.close();
			a.s.writeFreshness();
		} catch (TException e) {
			Log.e("refreshReports", "Could not download new reports!");
			e.printStackTrace();
		}
	}

	private void refreshMainReports(String uuid, String os, String model)
			throws TException {
		if (System.currentTimeMillis() - a.s.getFreshness() < FRESHNESS_TIMEOUT)
			return;
		if (c == null) {
			Log.e("refreshReports", "We are disconnected, not refreshing.");
			return;
		}
		Reports r = c.getReports(uuid, getFeatures("Model", model, "OS", os));
		// Assume multiple invocations, do not close
		// ProtocolClient.close();
		a.s.writeReports(r);
		// Assume freshness written by caller.
		// s.writeFreshness();
	}

	private void refreshBugReports(String uuid, String model) throws TException {
		if (System.currentTimeMillis() - a.s.getFreshness() < FRESHNESS_TIMEOUT)
			return;
		if (c == null) {
			Log.e("refreshReports", "We are disconnected, not refreshing.");
			return;
		}
		HogBugReport r = c.getHogOrBugReport(uuid,
				getFeatures("ReportType", "Bug", "Model", model));
		// Assume multiple invocations, do not close
		// ProtocolClient.close();
		a.s.writeBugReport(r);
		// Assume freshness written by caller.
		// s.writeFreshness();
	}

	private void refreshHogReports(String uuid, String model) throws TException {
		if (System.currentTimeMillis() - a.s.getFreshness() < FRESHNESS_TIMEOUT)
			return;
		if (c == null) {
			Log.e("refreshReports", "We are disconnected, not refreshing.");
			return;
		}
		HogBugReport r = c.getHogOrBugReport(uuid,
				getFeatures("ReportType", "Hog", "Model", model));

		// Assume multiple invocations, do not close
		// ProtocolClient.close();
		a.s.writeHogReport(r);
		// Assume freshness written by caller.
		// s.writeFreshness();
	}

	private List<Feature> getFeatures(String key1, String val1, String key2,
			String val2) {
		List<Feature> features = new ArrayList<Feature>();
		if (key1 == null || val1 == null || key2 == null || val2 == null) {
			Log.e("getFeatures", "Null key or value given to getFeatures!");
			System.exit(1);
			return features;
		}
		Feature feature = new Feature();
		feature.setKey(key1);
		feature.setValue(val1);
		features.add(feature);

		feature = new Feature();
		feature.setKey(key2);
		feature.setValue(val2);
		features.add(feature);
		return features;
	}
}