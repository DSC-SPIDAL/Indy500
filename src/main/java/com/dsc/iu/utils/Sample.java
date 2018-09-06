package com.dsc.iu.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.network.sensor.SensorParams.Keys;

import rx.Subscriber;

public class Sample {
	public Network network;
	public Publisher manualpublish;
	public static void main(String[] args) {
		Sample sample = new Sample();
		sample.runHTMNetwork();
		sample.explicitFileRead();
		System.out.println("completed running HTM code");
		
	}
	
	private void runHTMNetwork() {
//create a basic network here
        
        ConcurrentLinkedQueue<String> nbqueue = new ConcurrentLinkedQueue<String>();
		try {
			
			manualpublish = OnlineLearningUtils.getPublisher();
			Sensor<ObservableSensor<String[]>> sensor = Sensor.create(
	        	     ObservableSensor::create, 
	        	         SensorParams.create(
	        	             Keys::obs, new Object[] { "kakkerot", manualpublish }));
			
//			//BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/sample.csv")));
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream
//					("/Users/sahiltyagi/Desktop/Indy500/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
//			String line;
//			line = rdr.readLine(); line = rdr.readLine();			//skip line 1 and 2
//			while((line=rdr.readLine()) != null) {
//				//second condition to remove malformed time values in eRP log (or maybe these records hold different context)
//				if(line.startsWith("$P") && line.split("�")[2].length() >9) {
//					//System.out.println(line.split("\u00A6").length);
//					//nbqueue.add("5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3]);
//					manualpublish.onNext("5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3]);
//				}
//				
//				//manualpublish.onNext(line);
//			}
//			
//			rdr.close();
			
			//Network network = OnlineLearningUtils.createBasicLearningNetwork(manualpublish);
			Parameters p = OnlineLearningUtils.getLearningParameters();
			p = p.union(OnlineLearningUtils.getNetworkLearningEncoderParams());
			network =  Network.create("Network API Demo", p)
					.add(Network.createRegion("Region 1")
					.add(Network.createLayer("Layer 2/3", p)
					.alterParameter(KEY.AUTO_CLASSIFY, Boolean.TRUE)
					.add(Anomaly.create())
					.add(new TemporalMemory())
					.add(new SpatialPooler())
					.add(sensor)));
			
//			File outfile = new File("/Users/sahiltyagi/Desktop/htmsample.txt");
			File outfile = new File("/scratch_ssd/sahil/htmsample.txt");
			PrintWriter pw = new PrintWriter(new FileWriter(outfile));
			network.observe().subscribe(getSubscriber(outfile, pw));
			
			
			network.start();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	Subscriber<Inference> getSubscriber(File outputFile, PrintWriter pw) {
        return new Subscriber<Inference>() {
            @Override public void onCompleted() {
                System.out.println("\nstream completed. see output: " + outputFile.getAbsolutePath());
                try {
                    pw.flush();
                    pw.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            @Override public void onError(Throwable e) { e.printStackTrace(); }
            @Override public void onNext(Inference i) {
            		writeToFileAnomalyOnly(i, "consumption", pw); 
            	}
        };
    }
	
	private void writeToFileAnomalyOnly(Inference infer, String classifierField, PrintWriter pw) {
        try {
            if(infer.getRecordNum() > 0) {
                double actual = (Double)infer.getClassifierInput()
                        .get(classifierField).get("inputValue");
                StringBuilder sb = new StringBuilder()
                        .append(infer.getRecordNum()).append(",")
                        .append(String.format("%3.2f", actual)).append(",")
                        .append(infer.getAnomalyScore()).append(",")
                        .append(System.currentTimeMillis());
                pw.println(sb.toString());
                //pw.flush();
                //System.out.println(sb.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
            pw.flush();
        }
    }
	
	private void explicitFileRead() {
		try {
			
			//BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Desktop/sample.csv")));
			///scratch_ssd/sahil/
			BufferedReader rdr = new BufferedReader(new InputStreamReader(
								new FileInputStream("/scratch_ssd/sahil/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
//			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream
//					("/Users/sahiltyagi/Desktop/Indy500/eRPGenerator_TGMLP_20170528_Indianapolis500_Race.log")));
			
			//performance measurement
//			File f = new File("/Users/sahiltyagi/Desktop/Indy500/executionTime.txt");
			File f = new File("/scratch_ssd/sahil/executionTime.txt");
			PrintWriter p = new PrintWriter(f);
			int i=0;
			
			String line;
			line = rdr.readLine(); line = rdr.readLine();			//skip line 1 and 2
			manualpublish.onNext("5/28/17 00:00:00.000, 0.0");
			while((line=rdr.readLine()) != null) {
				//second condition to remove malformed time values in eRP log (or maybe these records hold different context)
				if(line.startsWith("$P") && line.split("�")[2].length() >9) {
					//System.out.println(line.split("\u00A6").length);
					//nbqueue.add("5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3]);
					
					//performance measurement
					i++;
					String val_pub = "5/28/17 " + line.split("�")[2] + "," + line.split("�")[line.split("�").length -3];
					String val_p = line.split("�")[line.split("�").length -3] + "," + System.currentTimeMillis();
					p.println(i + "," + val_p);
					manualpublish.onNext(val_pub);
					
					//input rate of 5 msg/sec
					try {
						Thread.sleep(200);
						
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				
				//manualpublish.onNext(line);
			}
			
			p.close();
			rdr.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
