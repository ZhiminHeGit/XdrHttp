import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenCellIdHbase {
    // 460 China
    // 452 vietnam
    // 454 Hong Kong
    // 455 Macau
    // 466 TW  No
    // 502 Malasiya
    // 525 Singapore No
    // 262 Germany
    // csv format available at http://wiki.opencellid.org/wiki/Menu_map_view#database
    public static void main(String[] args) throws IOException {
        Configuration config = HBaseConfiguration.create();
        config.clear();
        config.set("hbase.zookeeper.quorum", "localhost");
        config.set("hbase.zookeeper.property.clientPort","2181");
        config.set("hbase.master", "localhost:60000");
        byte[] columnFamily = Bytes.toBytes("data");
        byte[] column = Bytes.toBytes("gps");
        List<Put> puts = new ArrayList();

        Connection connection = null;
        Admin admin = null;
        try {
            connection = ConnectionFactory.createConnection(config);
            admin = connection.getAdmin();
            TableName tableName = TableName.valueOf("cell_map");
            if (admin.tableExists(tableName) == false) {
                HTableDescriptor desc = new HTableDescriptor(tableName);
                desc.addFamily(new HColumnDescriptor("data"));
                admin.createTable(desc);
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/Volumes/DataDisk/Data/cell_towers.csv"));
            String line = bufferedReader.readLine();
            Table table = connection.getTable(tableName);
            while (line != null) {
                CellTowerGPS cellTowerGps = new CellTowerGPS(line);
                Put put = new Put(Bytes.toBytes(cellTowerGps.getCellTower().toString()));
                put.addColumn(columnFamily, column,  Bytes.toBytes(cellTowerGps.getGps().toString()));
                puts.add(put);
                if (puts.size() > 10000) {
                    table.put(puts);
                    puts.clear();
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            admin.close();
            System.out.println("Done");
        }
     }
};
