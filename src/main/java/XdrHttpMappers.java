import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;

public class XdrHttpMappers {
    public static final FlatMapFunction<String, XdrHttp> XDRHTTP_EXTRACTOR =
            new FlatMapFunction<String, XdrHttp>() {
                @Override
                public Iterable<XdrHttp> call(String line) throws Exception {
                    XdrHttp xdrHttp = XdrHttp.parse(line);
                    return Arrays.asList(xdrHttp);
                }
            };
    public static final PairFunction<XdrHttp, Long, HttpUserDaily> IMSI_HTTPUSERDAILY_MAPPER =
            new PairFunction<XdrHttp, Long, HttpUserDaily>() {
                @Override
                public Tuple2<Long, HttpUserDaily> call(XdrHttp xdrHttp) throws Exception {
            //        HttpUserDaily httpUserDaily = processXdrHttpForHttpDaily(xdrHttp);
           //         return new Tuple2<>(xdrHttp.getImsi(), httpUserDaily);
                    return null;
                }
            };
}
