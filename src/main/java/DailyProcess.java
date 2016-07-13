import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public abstract class DailyProcess {
    final static int CHINA = 460;
    final static int TAIWAN = 466;
    final static int HONGKONG = 454;
    final static int ALL = 0;
    int count = 0;
    int skipCount = 0;
    int errCount = 0;
    String headLine = "no headline";
    DailyWriter writer;
    BufferedReader reader;
    int mcc = ALL;
    RecordType recordType = RecordType.XDR;

    public boolean process(XdrHttp xdrHttp) {
        System.err.println("not implemented");
        return false;
    }

    public boolean process(Sccp sccp) {
        System.err.println("not implemented");
        return false;
    }

    public void process(String input, String output, String[] args) {
        System.out.println(new Date() + " start processing");
        try {
            if (args.length == 2 || args.length == 3) {
                input = args[0];
                output = args[1];
            }
            System.out.println("input:" + input);
            System.out.println("output:" + output);
            writer = new DailyWriter(output);
            writer.write(getHeadLine(), false);
            if (input.toLowerCase().contains("hdfs://")) {
                FileSystem fs = FileSystem.get(new Configuration());
                FileStatus[] status = fs.listStatus(new Path(input));
                for (int i = 0; i < status.length; i++) {
                    if (!status[i].isDirectory()) {
                        InputStream inputStream = fs.open(status[i].getPath());
                        if (status[i].getPath().getName().endsWith(".gz")) {
                            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)));
                        } else {
                            reader = new BufferedReader(new InputStreamReader(inputStream));
                        }
                        process();
                    }
                }
            } else {
                File inputFile = new File(input);
                File[] files;
                if (inputFile.isDirectory()) {
                    files = inputFile.listFiles();
                } else {
                    files = new File[1];
                    files[0] = inputFile;
                }
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    System.out.println("processing:" + file);
                    if (file.getName().endsWith("gz")) {
                        InputStream fileStream = new FileInputStream(file.getCanonicalPath());
                        InputStream gzipStream = new GZIPInputStream(fileStream);
                        Reader decoder = new InputStreamReader(gzipStream);
                        reader = new BufferedReader(decoder);
                    } else {
                        reader = new BufferedReader(new FileReader(file));
                    }
                    process();
                    reader.close();
                }

            }
            writeOut(false);
            reader.close();
            writer.close();
            System.out.println("processed total:" + count);
            System.out.println("skiped:" + errCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(new Date() + " finish processing");
    }

    private void process() throws IOException {
        String line = reader.readLine();
        line = reader.readLine();
        //String separator = ",";
        // if (line != null && line.contains("\t")) {
        String separator = "\t";
        //}
        while (line != null) {
            count++;
            if (count % 100000 == 0) {
                System.out.println(new Date() + " " + count + " " + skipCount);
            }
            if (count >= 1000000) {
                writer.close();
                System.exit(0);
            }
            if (recordType == RecordType.XDR) {
                XdrHttp xdrHttp = XdrHttp.parse(line, separator);
                if (xdrHttp != null) {

                    if (mcc == 0 || xdrHttp.getMcc() == mcc) {
                        //writer.write(line + "\n", false);
                        if (!process(xdrHttp)) break;
                    } else {
                        skipCount++;
                    }
                } else {
                    System.err.println("Wrong record:" + line);
                    errCount++;
                }
            } else {
                Sccp sccp = Sccp.parse(line);
                if (sccp != null) {
                    if (!process(sccp)) break;
                } else {
                    errCount++;
                }
            }
            line = reader.readLine();
        }
    }

    public void writeOut(boolean print_to_screan) {
        System.err.println("not implemented");
    }

    public String getHeadLine() {
        return headLine;
    }

    enum RecordType {
        XDR,
        SCCP
    }

}
