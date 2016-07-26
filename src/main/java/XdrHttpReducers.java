import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.mapreduce.v2.app.webapp.App;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class XdrHttpReducers {

    public static final Function2<AppStats, AppStats, AppStats> APP_STATS_REDUCER = new Function2<AppStats, AppStats, AppStats>() {
        @Override
        public AppStats call(AppStats appStats, AppStats appStats2) throws Exception {
            AppStats result = new AppStats();
            result.count = appStats.count + appStats2.count;
            result.contentLength = appStats.contentLength + appStats2.contentLength;
            result.duration = appStats.duration + appStats2.duration;
            result.agentSet.addAll(appStats.agentSet);
            result.agentSet.addAll(appStats2.agentSet);
            return result;
        }
    };

    public static final Function2<ResponseCodeStats, ResponseCodeStats, ResponseCodeStats> RESPONSE_CODE_STATS_REDUCER =
            new Function2<ResponseCodeStats, ResponseCodeStats, ResponseCodeStats>() {
        @Override
        public ResponseCodeStats call(ResponseCodeStats responseCodeStats, ResponseCodeStats responseCodeStats2) throws Exception {
            ResponseCodeStats result = new ResponseCodeStats();
            result.zero = responseCodeStats.zero + responseCodeStats2.zero;
            result.information = responseCodeStats.information + responseCodeStats2.information;
            result.success = responseCodeStats.success + responseCodeStats2.success;
            result.redirection = responseCodeStats.redirection + responseCodeStats2.redirection;
            result.client_error = responseCodeStats.client_error + responseCodeStats2.client_error;
            result.server_error = responseCodeStats.server_error + responseCodeStats2.server_error;
            result.other_error = responseCodeStats.other_error + responseCodeStats2.other_error;
            return result;
        }
    };



}