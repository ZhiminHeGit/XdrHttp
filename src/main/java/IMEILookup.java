import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by usrc on 5/17/2016.
 */
public class IMEILookup {

    // Rate control this function to avoid being blocked by the site.
    public static String lookupIMEI(long imei) {
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        try {
            url = new URL("http://imeidata.net/check?imei=" + imei);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                if (line.contains(("Model:"))) {
                    line = br.readLine().replace("<span class=\"value\">", "").replace("</span>", "");
                    return line;
                }
            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        return "not valid";
    }

    public static void main(String[] args) throws IOException {
        System.out.println(lookupIMEI(354435068440627L));
    }
}
