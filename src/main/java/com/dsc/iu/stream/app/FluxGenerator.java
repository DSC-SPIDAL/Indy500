package com.dsc.iu.stream.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class FluxGenerator {

    private static final String TEMPLATE_SPOUT = "  - id: \"telemetryspout-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.IndycarLatency\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
            "      - \"#NO\"\n\n";

    private static final String TEMPLATE_BOLTS = "  - id: \"RPMBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricLatency\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
//            "      - \"#NO\"\n" +
            "      - \"RPM\"\n" +
            "      - \"-3000\"\n" +
            "      - \"15000\"\n" +
            "      \n" +
            "  - id: \"SpeedBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricLatency\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
//            "      - \"#NO\"\n" +
            "      - \"speed\"\n" +
            "      - \"-20\"\n" +
            "      - \"250\"\n" +
            "      \n" +
            "  - id: \"ThrottleBolt-#NO\"\n" +
            "    className: \"com.dsc.iu.stream.app.ScalarMetricLatency\"\n" +
            "    parallelism: 1\n" +
            "    constructorArgs:\n" +
//            "      - \"#NO\"\n" +
            "      - \"throttle\"\n" +
            "      - \"0\"\n" +
            "      - \"30\"\n\n";

    private static final String TEMPLATE_STREAMS = "  - name: \"telemetryspout-#NO --> RPMBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"RPMBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"telemetryspout-#NO --> SpeedBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"SpeedBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"telemetryspout-#NO --> ThrottleBolt-#NO\"\n" +
            "    from: \"telemetryspout-#NO\"\n" +
            "    to: \"ThrottleBolt-#NO\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "     \n" +
            "  - name: \"RPMBolt-#NO --> sink\"\n" +
            "    from: \"RPMBolt-#NO\"\n" +
            "    to: \"sink\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"SpeedBolt-#NO --> sink\"\n" +
            "    from: \"SpeedBolt-#NO\"\n" +
            "    to: \"sink\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n" +
            "      \n" +
            "  - name: \"ThrottleBolt-#NO --> sink\"\n" +
            "    from: \"ThrottleBolt-#NO\"\n" +
            "    to: \"sink\"\n" +
            "    grouping:\n" +
            "      type: SHUFFLE\n\n";

    //private static final int NO_OF_CARS = 33;

    public static void main(String[] args) throws IOException {
    	List<String> carlist = new LinkedList<String>();
    	carlist.add("20");carlist.add("21");carlist.add("13");carlist.add("98");carlist.add("19");carlist.add("33");carlist.add("24");carlist.add("26");
    	
//    	carlist.add("7");carlist.add("6");
//    	carlist.add("60");carlist.add("27");carlist.add("22");carlist.add("18");carlist.add("3");carlist.add("4");carlist.add("28");carlist.add("32");carlist.add("59");carlist.add("25");carlist.add("64");carlist.add("10");carlist.add("15");carlist.add("17");
//		carlist.add("12");carlist.add("1");carlist.add("9");carlist.add("14");carlist.add("23");carlist.add("30");carlist.add("29");carlist.add("88");carlist.add("66");
    	
        StringBuilder fluxTemplate = new StringBuilder(
                "name: \"indy500-latency\"\n" +
                        "config:\n" +
                        "  topology.workers: 5\n\n"
        );

        StringBuilder spouts = new StringBuilder("spouts:\n");
        StringBuilder bolts = new StringBuilder("bolts:\n");
        bolts.append("  - id: \"sink\"\n" +
                "    className: \"com.dsc.iu.stream.app.Sink\"\n" +
                "    parallelism: 1\n\n");
        StringBuilder streams = new StringBuilder("streams:\n");

        for (int i = 0; i < carlist.size(); i++) {
            spouts.append(
                    TEMPLATE_SPOUT.replaceAll("#NO", carlist.get(i))
            );

            bolts.append(
                    TEMPLATE_BOLTS.replaceAll("#NO", carlist.get(i))
            );

            streams.append(
                    TEMPLATE_STREAMS.replaceAll("#NO", carlist.get(i))
            );
        }

        fluxTemplate.append(spouts);
        fluxTemplate.append("\n");
        fluxTemplate.append(bolts);
        fluxTemplate.append("\n");
        fluxTemplate.append(streams);

        FileWriter fileWriter = new FileWriter(new File("/Users/sahiltyagi/Desktop/indylatency.yaml"));
        fileWriter.write(fluxTemplate.toString());
        fileWriter.flush();
        fileWriter.close();
        System.out.println(fluxTemplate);
        System.out.println("list size:"+carlist.size());
    }
}