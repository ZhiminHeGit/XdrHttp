import java.util.HashSet;

/**
 * Created by zhiminhe on 11/9/16.
 */
public class Utils {
    static HashSet newHashSet(long e) {
        HashSet<Long> set = new HashSet();
        set.add(e);
        return set;
    }
}
