import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HBaseProcessApp extends HBaseProcess {
    AppRules appRules = new AppRules();

    public static void main(String[] args) throws IOException {
        HBaseProcessApp hBaseProcess = new HBaseProcessApp();
        String[] dateList = {"20161001", "20161002", "20161003", "20161004", "20161005",
                "20161006", "20161007",
                "20170117", "20170118", "20170128", "20170129", "20170130", "20170131",
                "20170201"};
        hBaseProcess.processAll(dateList, "app_");
    }

    @Override
    public boolean processRecord(XdrHttp xdrHttp) {
        String url = xdrHttp.getHost() + xdrHttp.getUriData();
        String app = appRules.getCMIApp(url);
        if (app == AppRules.NO_MATCH) {
            app = xdrHttp.getHost();
        }
        Put put = new Put(Bytes.toBytes(String.format("%d:%d",
                xdrHttp.getMsisdn(), xdrHttp.getHomeMcc())));
        put.addColumn(columnFamily, c(app + ":" + xdrHttp.getMcc()), Bytes.toBytes(1));
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
