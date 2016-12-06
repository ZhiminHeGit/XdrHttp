import java.util.HashSet;
import java.util.Set;

/**
 * Created by usrc on 5/23/2016.
 */
public class ContentTypeStats {
    int count = 0;
    int durationMS = 0;
    int contentLength = 0;
    Set<String> agentSet = new HashSet();
    Set<String> filteredHostSet = new HashSet();

    public void addCtUsage(VipLine vipLine) {
        count++;
        durationMS += vipLine.getDurationMS();
        contentLength += vipLine.getContentLength();
        agentSet.add(vipLine.getUserAgent());
        filteredHostSet.add(vipLine.getFilteredHost());
    }

    public String toString() {
        return count + "," + durationMS + "," + contentLength + "," +
                agentSet.size() + "," + filteredHostSet.size();
    }
}