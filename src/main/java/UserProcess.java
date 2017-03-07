import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhiminhe on 1/30/17.
 */

// 460 china, 466 tw, 452 vietnam, 454 Hongkong,455 Macau,502 Malasiya,525 Singapore
public class UserProcess extends DailyProcess {
    Map<Long, Integer> outboundUserCountMap = new HashMap();
    Map<Long, Integer> inboundUserCountMap = new HashMap();
    @Override
    public boolean process(XdrHttp xdrHttp) {
        if (xdrHttp.getHomeMcc() == 460) {
            if (!outboundUserCountMap.containsKey(xdrHttp.getImsi())) {
                outboundUserCountMap.put(xdrHttp.getImsi(), 0);
            }
            outboundUserCountMap.put(xdrHttp.getImsi(), outboundUserCountMap.get(xdrHttp.getImsi()) +1);
        } else {
            if (!inboundUserCountMap.containsKey(xdrHttp.getImsi())) {
                inboundUserCountMap.put(xdrHttp.getImsi(), 0);
            }
            inboundUserCountMap.put(xdrHttp.getImsi(), inboundUserCountMap.get(xdrHttp.getImsi()) +1);
        }
        return true;
    }
}
