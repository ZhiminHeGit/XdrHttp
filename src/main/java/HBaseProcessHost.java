import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created by zhiminhe on 2/10/17.
 */
public class HBaseProcessHost extends HBaseProcess {
    public static void main(String[] args) throws IOException {
        HBaseProcessHost hBaseProcess = new HBaseProcessHost();
        String[] dateList = {"20161001", "20161002", "20161003", "20161004", "20161005", "20161006", "20161007",
                "20170117", "20170118", "20170128", "20170129", "20170130", "20170131",
                "20170201"};
        hBaseProcess.processAll(dateList, "host_");
    }

    @Override
    public boolean processRecord(XdrHttp xdrHttp) {
        Put put = new Put(Bytes.toBytes(String.format("%d:%d",
                xdrHttp.getMsisdn(), xdrHttp.getHomeMcc())));
        put.addColumn(columnFamily, c(xdrHttp.getHost() + ":" + xdrHttp.getMcc()), Bytes.toBytes(1));
        try {
            if (!put.isEmpty()) {
                puts.add(put);
                if (puts.size() > 10000) {
                    table.put(puts);
                    puts.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
