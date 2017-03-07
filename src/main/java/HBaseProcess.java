import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HBaseProcess {
    Connection connection = null;
    Table table = null;
    List<Put> puts = new ArrayList();
    byte[] columnFamily = Bytes.toBytes("data");
    byte[] column = Bytes.toBytes("raw");
    Configuration config;
    Admin admin;

    public byte[] c(String column) {
        return Bytes.toBytes(column);
    }

    public HBaseProcess() {
        config = HBaseConfiguration.create();
        config.clear();
        config.set("hbase.zookeeper.quorum", "localhost");
        config.set("hbase.zookeeper.property.clientPort", "2181");
        config.set("hbase.master", "localhost:60000");
        try {
            connection = ConnectionFactory.createConnection(config);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectOrCreateTable(String tableNameString) {
        try {
            TableName tableName = TableName.valueOf(tableNameString);
            System.out.println("Recreating Table: " + tableNameString);
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
            }
            HTableDescriptor desc = new HTableDescriptor(tableName);
            desc.addFamily(new HColumnDescriptor("data"));
            admin.createTable(desc);

            if (table != null) {
                table.close();
            }
            table = connection.getTable(tableName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processAll(String[] dateList, String prefix) {
        try {

            for (String date : dateList) {
                File dataFolder = new File("/Volumes/WDPassport/CMIData/" + date);
                if (dataFolder.isDirectory()) {
                    connectOrCreateTable(prefix + dataFolder.getName());
                    for (File file : dataFolder.listFiles()) {
                        if (file.isFile()) {
                            System.out.println("Processing:" + file.getName());
                            BufferedReader reader = new BufferedReader(
                                    new FileReader(file));
                            String line = reader.readLine();
                            while (line != null) {
                                XdrHttp xdrHttp = XdrHttp.parse(line);
                                if (xdrHttp != null) {
                                    processRecord(xdrHttp);
                                }
                                line = reader.readLine();
                            }
                            reader.close();
                        }
                    }
                }
            }
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        HBaseProcess hBaseProcess = new HBaseProcess();
        String[] dateList = {"20161005", "20161006", "20161007",
                "20170117", "20170118", "20170128", "20170129", "20170130", "20170131",
                "20170201"};
        hBaseProcess.processAll(dateList, "xdr_http_");
    }

    public boolean processRecord(XdrHttp xdrHttp) {
        Put put = new Put(Bytes.toBytes(String.format("%d:%d", xdrHttp.getMsisdn(), xdrHttp.getDate())));
        put.addColumn(columnFamily, column, Bytes.toBytes(xdrHttp.getRaw()));
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