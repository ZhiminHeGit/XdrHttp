import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class SparkMain {

    // some constants
    private static final String OUTPUT_ROOTPATH = "/data/output/tn_signal/xdr_http/20160221/";
    private static final String HOSTS = "HOSTS";
    private static final String HTTP_USER_DAILY = "HttpUserDaily";
    private static final String CONSOLIDATED_HOSTS = "CONSOLIDATED_HOSTS";
    private static final String DATA_CONSUMPTION = "DATA_CONSUMPTION";
    private static final String ROAMING_DATA_CONSUMPTION = "ROAMING_DATA_CONSUMPTION";
    private static final String ROAMING_DATA_PER_USER_CONSUMPTION = "ROAMING_DATA_PER_USER_CONSUMPTION";
    private static final String ROAMING_DATA_PER_USER_CONSUMPTION_SORTED = "ROAMING_DATA_PER_USER_CONSUMPTION_SORTED";
    private static final String FOOTPRINT = "FOOTPRINT";
    private static final String ROAMING_USERS = "ROAMING_USERS";

    public static String separator = ",";
    private static String SEPARATOR = System.getProperty("file.separator");

    static class MyComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 5462223600l;

        public int compare(String a, String b) {
            int index1 = getIndex(a);
            int index2 = getIndex(b);

            if (index1 == -1 || index2 == -1)
                return 0;

            int cmpValue = (a.substring(0, index1 - 1)).compareTo(b.substring(0, index2 - 1));
            if (cmpValue == 0) {
                String[] parts = a.split(",");
                Long a1 = Long.parseLong(parts[parts.length - 1]);
                parts = b.split(",");
                Long b1 = Long.parseLong(parts[parts.length - 1]);

                return b1.compareTo(a1);
            }

            return cmpValue;
        }

        private int getIndex(String str) {
            if (str == null || str.isEmpty())
                return -1;
            int count = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == ',')
                    count++;
                if (count == 3)
                    return i;
            }

            return -1;
        }
    }

    public static void main(String[] args) {
        // expects 2 input parameters
        if (args.length < 2) {
            System.out.println("Usage: spark-submit --class com.cmti.cmiSpark.XdrAnalysis --master local[32] XdrAnalysis-0.1.jar input-file-path output-folder [separator]");
            System.out.println("For example: spark-submit --class com.cmti.cmiSpark.XdrAnalysis --master local[32] XdrAnalysis-0.1.jar /data/input/tn_signal/xdr_http/20160221/ /data/output/tn_signal/xdr_http/20160221/ COMMA|TAB");
            System.out.println("For now the separator can either be a COMMA or a TAB.");
            System.out.println("The default separator is COMMA if not passing a separator parameter in command line.");

            System.exit(1);
        }

        if (args.length == 3) {
            switch (args[2]) {
                case "TAB":
                    separator = "\t";
                    break;
                default:
                    separator = ",";
                    break;
            }
        }

        // Spark configuration and context
        SparkConf conf = new SparkConf().setAppName("XdrHttp");
        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> file = context.textFile(args[0]);

        JavaRDD<XdrHttp> xdrHttpJavaRDD = file.flatMap(XdrHttpMappers.XDRHTTP_EXTRACTOR);

        JavaPairRDD<Long, HttpUserDaily> userDailyJavaPairRDD = xdrHttpJavaRDD.mapToPair(XdrHttpMappers.IMSI_HTTPUSERDAILY_MAPPER);
        JavaPairRDD<Long, HttpUserDaily> userDailyJavaPairRDDResults = userDailyJavaPairRDD.reduceByKey(XdrHttpReducers.HTTPUSERDAILY_REDUCER).sortByKey();
        long curDate = new Date().getTime();
        userDailyJavaPairRDDResults.saveAsTextFile(args[1] + curDate + SEPARATOR + HTTP_USER_DAILY);

      //  JavaPairRDD<Long, List<FootPrint>> footPrintResults = userDailyJavaPairRDDResults.values().mapToPair(XdrHttpMappers.IMSI_FOOTPRINT_MAPPER);
     //   footPrintResults.saveAsTextFile(args[1] + curDate + SEPARATOR + FOOTPRINT);
    }
}