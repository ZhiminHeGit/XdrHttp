import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;

public class XdrHttpMappers {
    static AppRules appRules = new AppRules();
    public static final FlatMapFunction<String, XdrHttp> XDRHTTP_EXTRACTOR =
            new FlatMapFunction<String, XdrHttp>() {
                @Override
                public Iterable<XdrHttp> call(String line) throws Exception {
                    XdrHttp xdrHttp = XdrHttp.parse(line);
                    return Arrays.asList(xdrHttp);
                }
            };
    public static final PairFunction<XdrHttp, String, AppStats> IMSI_APP_STATS_MAPPER =
            new PairFunction<XdrHttp, String, AppStats>() {
                @Override
                public Tuple2<String, AppStats> call(XdrHttp xdrHttp) throws Exception {
                    String key = xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," + xdrHttp.getMcc() + "," + xdrHttp.getHost() + "," +
                            xdrHttp.getFilteredHost() + "," + appRules.getCMIApp(xdrHttp.getHost());
                    AppStats appStats = new AppStats();
                    appStats.addAppUsage(xdrHttp);
                    return new Tuple2(key, appStats);
                }
            };
    public static final PairFunction<XdrHttp, String, ResponseCodeStats> IMSI_RESPONSE_CODE_STATS_MAPPER =
            new PairFunction<XdrHttp, String, ResponseCodeStats>() {
                @Override
                public Tuple2<String, ResponseCodeStats> call(XdrHttp xdrHttp) throws Exception {
                    String key = xdrHttp.getImsi() + "," + xdrHttp.getReadableDate() + "," +
                            xdrHttp.getMcc() + "," + xdrHttp.getHost() + "," + xdrHttp.getTai() + ","
                            + xdrHttp.getEcgi() + "," + xdrHttp.getCellLAC() + "," + xdrHttp.getCellCI();
                    ResponseCodeStats responseCodeStats = new ResponseCodeStats();
                    responseCodeStats.addCode(Integer.toString(xdrHttp.getResponoseCode()));
                    return new Tuple2(key, responseCodeStats);
                }
            };
}
