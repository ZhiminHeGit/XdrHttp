import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Date;

public class SparkMain {

    private static final String APP_STATS_DAILY = "AppStatsDaily";
    private static final String RESPONSE_CODE_DAILY = "ResponseCodeDaily";

    public static String separator = ",";
    private static String SEPARATOR = System.getProperty("file.separator");

    public static void main(String[] args) {
        // expects 2 input parameters
        if (args.length < 2) {
            System.out.println("Usage: spark-submit --class SparkMain --master local[32] XdrAnalysis-0.1.jar input-file-path output-folder [separator]");
            System.out.println("For example: spark-submit --class SparkMain --master local[32] XdrAnalysis-0.1.jar /data/input/tn_signal/xdr_http/20160221/ /data/output/tn_signal/xdr_http/20160221/ COMMA|TAB");
            System.out.println("For now the separator can either be a COMMA or a TAB.");
            System.out.println("The default separator is COMMA if not passing a separator parameter in command line.");
            System.exit(1);
        }

        if (args.length == 3) {
            if (args[2] == "TAB") {
                separator = "\t";
            } else {
                separator = ",";
            }
        }

        // Spark configuration and context
        SparkConf conf = new SparkConf().setAppName("AppStats");
        JavaSparkContext context = new JavaSparkContext(conf);

        JavaRDD<String> file = context.textFile(args[0]);

        JavaRDD<XdrHttp> xdrHttpJavaRDD = file.flatMap(XdrHttpMappers.XDRHTTP_EXTRACTOR);

        JavaPairRDD<String, AppStats> appStatsJavaPairRDD = xdrHttpJavaRDD.mapToPair(XdrHttpMappers.IMSI_APP_STATS_MAPPER);
        JavaPairRDD<String, AppStats> appStatsRDDResults = appStatsJavaPairRDD.reduceByKey(XdrHttpReducers.APP_STATS_REDUCER).sortByKey();

        JavaPairRDD<String, ResponseCodeStats> responseCodeStatsJavaPairRDD = xdrHttpJavaRDD.mapToPair(XdrHttpMappers.IMSI_RESPONSE_CODE_STATS_MAPPER);
        JavaPairRDD<String, ResponseCodeStats> responseCodeRDDResults = responseCodeStatsJavaPairRDD.reduceByKey(XdrHttpReducers.RESPONSE_CODE_STATS_REDUCER).sortByKey();

        long curDate = new Date().getTime();
        appStatsRDDResults.saveAsTextFile(args[1] + curDate + SEPARATOR + APP_STATS_DAILY);
        responseCodeRDDResults.saveAsTextFile(args[1] + curDate + SEPARATOR + RESPONSE_CODE_DAILY);
    }
}