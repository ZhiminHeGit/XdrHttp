/*import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path; */

import java.io.*;

/**
 * Created by usrc on 5/27/2016.
 */
public class DailyWriter {
    BufferedWriter bufferedWriter;
    boolean isHdfs =false;
    // FileSystem hdfs;

    public DailyWriter(String filename) {
        try {
         /*   if(filename.toLowerCase().contains("hdfs://")) {
                isHdfs = true;
                Configuration configuration = new Configuration();
                hdfs = FileSystem.get(configuration);
                Path file = new Path(filename);
                if (hdfs.exists(file)) {
                    hdfs.delete(file, true);
                }
                OutputStream os = hdfs.create(file);
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            } else { */
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(filename)));
          //  }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String line, boolean print) {
        if (print) {
            System.out.print(line);
        }
        try {
            bufferedWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            bufferedWriter.close();
            if (isHdfs) {
        //        hdfs.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
