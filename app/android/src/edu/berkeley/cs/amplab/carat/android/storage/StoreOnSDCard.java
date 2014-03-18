package edu.berkeley.cs.amplab.carat.android.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.os.Environment;
import edu.berkeley.cs.amplab.carat.thrift.Sample;

public class StoreOnSDCard {
	
	public static void writeSamples(Context c, Collection<Sample> samples) throws IOException{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/com.hoque.systemmonitor");
		if (!dir.exists())
			dir.mkdirs();
		
		for (Sample sample : samples) {
			long time = (long) sample.getTimestamp();
			HashMap<String, String> m = SampleReader.writeSample(sample);
			Set<String> keys = m.keySet();

			File logFile = new File(dir, "systemmonitor-" + time + ".txt");
			FileOutputStream foutStream = new FileOutputStream(logFile);

			for (String k : keys) {
				String s = k + "=" + m.get(k).replaceAll("\n", "§") + "\n";
				foutStream.write(s.getBytes());
			}
			foutStream.close();
		}
	}
	
	public static void prepareAndWriteLogFile(Context c, Sample sample) throws IOException{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/com.hoque.systemmonitor");
		if (!dir.exists())
			dir.mkdirs();
		
		long time = (long) sample.getTimestamp();
		HashMap<String, String> m = SampleReader.writeSample(sample);
		Set<String> keys = m.keySet();

		File logFile = new File(dir, "systemmonitor-"+time+".txt");
		FileOutputStream foutStream = new FileOutputStream(logFile);

		for (String k: keys){
			String s = k+"="+m.get(k).replaceAll("\n", "§")+"\n";
			foutStream.write(s.getBytes());
		}
		foutStream.close();
	}
}
