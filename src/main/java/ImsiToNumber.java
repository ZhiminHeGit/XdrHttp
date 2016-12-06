import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by usrc on 6/22/2016.
 */
public class ImsiToNumber {
    Map<Long, Long> imsiToNumberMap = new HashMap();
public ImsiToNumber(String imsiToNumberMapFile) {
    String line = "";
    try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(imsiToNumberMapFile));
        line = bufferedReader.readLine();
        // skip first line
        line = bufferedReader.readLine();
        while (line != null) {
            String[] strs = line.split(";");
            imsiToNumberMap.put(Long.parseLong(strs[1]), Long.parseLong(strs[0]));
            line = bufferedReader.readLine();
        }
    } catch (IOException e) {
        System.err.println(line);
        e.printStackTrace();
    }
}

    public Long lookup(long imsi) {
        return imsiToNumberMap.get(imsi);
    }
}
