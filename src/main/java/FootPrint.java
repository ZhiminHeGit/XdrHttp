import java.io.Serializable;

public class FootPrint implements Serializable {
    long imsi;
    long enterDate;
    long exitDate;

    long dataConsumption;
    int servingMcc;

    public FootPrint() {
    }

    public long getDataConsumption() {
        return dataConsumption;
    }

    public void setDataConsumption(long dataConsumption) {
        this.dataConsumption = dataConsumption;
    }

    public long getImsi() {
        return imsi;
    }

    public void setImsi(long imsi) {
        this.imsi = imsi;
    }

    public long getEnterDate() {
        return enterDate;
    }

    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    public long getExitDate() {
        return exitDate;
    }

    public void setExitDate(long exitDate) {
        this.exitDate = exitDate;
    }

    public int getServingMcc() {
        return servingMcc;
    }

    public void setServingMcc(int servingMcc) {
        this.servingMcc = servingMcc;
    }
}
