import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by usrc on 5/27/2016.
 */
public class DailyReaderWriter {
    BufferedReader reader;
    DailyWriter writer;

    public DailyReaderWriter(String input, String output, String[] args) {
        try {
            if (args.length  == 2 || args.length ==3) {
                input = args[0];
                output = args[1];
            }

            System.out.println("input:" + input);
            System.out.println("output:" + output);
            if (output.endsWith("gz")) {
                InputStream fileStream = new FileInputStream(output);
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream);
                reader = new BufferedReader(decoder);
            } else {
                reader = new BufferedReader(new FileReader(input));
            }
            writer = new DailyWriter(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getReader() {
        return reader;
    }

    public DailyWriter getWriter() {
        return writer;
    }
}
