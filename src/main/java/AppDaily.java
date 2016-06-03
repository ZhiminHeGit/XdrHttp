/**
 * Created by usrc on 5/27/2016.
 */

// java -cp XdrHttp-1.0-SNAPSHOT.jar Daily  /Users/jianli/Downloads/cmidata/xdr_http/raw/http0501.csv /root/all_user_app_daily.csv
public class AppDaily {
    public static void main(String[] args) {
        Daily daily = new Daily();
        AppProcess appProcess = new AppProcess();
        daily.process("C:\\xdr_http\\51sample.csv", "\t",
                "C:\\xdr_http\\all_user_app_daily.csv",
                args, appProcess);
    }
}
