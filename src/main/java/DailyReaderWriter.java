import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by usrc on 5/27/2016.
 */
public class DailyReaderWriter {
    BufferedReader reader;
    DailyWriter writer;

    public DailyReaderWriter(String input, String output, String[] args) {
        try {
            if (args.length == 2) {
                input = args[0];
                output = args[1];
            }
            System.out.println("input:" + input);
            System.out.println("output:" + output);
            reader = new BufferedReader(new FileReader(input));
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
