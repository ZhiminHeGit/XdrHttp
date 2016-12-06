import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by usrc on 5/23/2016.
 */
public class AppStats implements Serializable {
    long count = 0;
    long contentLength = 0;
    long duration = 0;
    Set<String> agentSet = new HashSet();

    public void addAppUsage(VipLine vipLine) {
        count++;
        contentLength += vipLine.getContentLength();
        duration += vipLine.getDurationMS();
        agentSet.add(vipLine.getUserAgent());
    }

    public void addAppUsage(XdrHttp xdrHttp) {
        count++;
        contentLength += xdrHttp.getContentLength();
        duration += xdrHttp.getDuration();
        agentSet.add(xdrHttp.getUserAgent());
    }

    public String toString() {
        String output = "";
        for (String agent : agentSet
                ) {
            if (output == "") {
                output = agent;
            } else {
                output = output + ";" + agent;
            }
        }
        return count + "," + contentLength + "," + duration + "," + output;
    }
}
