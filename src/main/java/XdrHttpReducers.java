import org.apache.commons.lang3.tuple.Pair;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class XdrHttpReducers {
    public static final Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple2<Long, Long>> FOOTPRINT_REDUCER =
            new Function2<Tuple2<Long, Long>, Tuple2<Long, Long>, Tuple2<Long, Long>>() {
                @Override
                public Tuple2<Long, Long> call(Tuple2<Long, Long> a, Tuple2<Long, Long> b) throws Exception {
                    return new Tuple2<Long, Long>(a._1.longValue() < b._1.longValue() ? a._1 : b._1, a._2.longValue() > b._2.longValue() ? a._2 : b._2);
                }
            };

    public static final Function2<Long, Long, Long> LONG_ADDITION_REDUCER =
            new Function2<Long, Long, Long>() {
                @Override
                public Long call(Long a, Long b) throws Exception {
                    return a + b;
                }
            };

    public static final Function2<Integer, Integer, Integer> INTEGER_ADDITION_REDUCER =
            new Function2<Integer, Integer, Integer>() {
                @Override
                public Integer call(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            };

    public static final Function2<HttpUserDaily, HttpUserDaily, HttpUserDaily> HTTPUSERDAILY_REDUCER =
            new Function2<HttpUserDaily, HttpUserDaily, HttpUserDaily>() {
                @Override
                public HttpUserDaily call(HttpUserDaily a, HttpUserDaily b) throws Exception {
                    HttpUserDaily result = new HttpUserDaily();
                    result.setImsi(a.getImsi());
                    result.setDate(Math.max(a.getDate(), b.getDate()));

                    result.setImeis(mergeList(a.getImeis(), b.getImeis()));
                    result.setHomeMcc(a.getHomeMcc());
                    result.setHomeMnc(a.getHomeMnc());

                    HashSet<Pair<Integer, Integer>> pairSet = new HashSet<>();
                    pairSet.addAll(a.getVisitingMccMncPairs());
                    pairSet.addAll(b.getVisitingMccMncPairs());

                    result.setVisitingMccMncPairs(pairSet);

                    result.setDuration_3G(a.getDuration_3G() + b.getDuration_3G());
                    result.setContent_length_3G(a.getContent_length_3G() + b.getContent_length_3G());
                    result.setNum_request_3G(a.getNum_request_3G() + b.getNum_request_3G());
                    result.setDuration_4G(a.getDuration_4G() + b.getDuration_4G());
                    result.setContent_length_4G(a.getContent_length_4G() + b.getContent_length_4G());
                    result.setNum_request_4G(a.getNum_request_4G() + b.getNum_request_4G());

                    HashSet<String> agentSet = new HashSet<>();
                    agentSet.addAll(a.getAgentSet());
                    agentSet.addAll(b.getAgentSet());
                    result.setAgentSet(agentSet);
                    HashSet<String> androidSet = new HashSet<>();
                    androidSet.addAll(a.getAndroidSet());
                    androidSet.addAll(b.getAndroidSet());
                    result.setAndroidSet(androidSet);

                    result.setVideo_length(a.getVideo_length() + b.getVideo_length());
                    result.setTotal_length(a.getTotal_length() + b.getTotal_length());

                    List<FootPrint> footPrints = mergeList(a.getFootPrints(), b.getFootPrints());
                  //  Collections.sort(footPrints, new FootPrintComparator());

                    FootPrint lastFootPrint = footPrints.get(0);
                    List<FootPrint> mergedPrints = new ArrayList<>();
                    /*for(int i = 1; i < footPrints.size(); i++){
                        FootPrint current = footPrints.get(i);
                        if(lastFootPrint.getServingNetwork().equals(current.getServingNetwork())){
                            lastFootPrint.setEnterDate(Math.min(lastFootPrint.getEnterDate(), current.getEnterDate()));
                            lastFootPrint.setLastSeenDate(Math.max(lastFootPrint.getLastSeenDate(), current.getLastSeenDate()));
                            lastFootPrint.setDataConsumption(lastFootPrint.getDataConsumption() + current.getDataConsumption());
                        } else {
                            // network transition
                          //  lastFootPrint.setExitDate(lastFootPrint.getLastSeenDate());
                            mergedPrints.add(lastFootPrint);
                            lastFootPrint = current;
                        }
                    }*/

                    mergedPrints.add(lastFootPrint);
                    result.setFootPrints(mergedPrints);
                    return result;
                }
            };

    private static <T> List<T> mergeList(List<T> t1, List<T> t2){
        HashSet<T> set = new HashSet<>();
        set.addAll(t1);
        set.addAll(t2);

        List<T> tList = new ArrayList<>();
        for(T t:set)
            tList.add(t);

        return tList;
    }
}