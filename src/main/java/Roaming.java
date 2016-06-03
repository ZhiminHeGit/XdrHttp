public class Roaming {
    int homeMcc, servingMcc;

    public Roaming(int homeMcc, int servingMcc) {
        this.homeMcc = homeMcc;
        this.servingMcc = servingMcc;
    }

    public Roaming() {
    }

    public int getHomeMcc() {
        return homeMcc;
    }

    public void setHomeMcc(int homeMcc) {
        this.homeMcc = homeMcc;
    }

    public int getServingMcc() {
        return servingMcc;
    }

    public void setServingMcc(int servingMcc) {
        this.servingMcc = servingMcc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Roaming)) return false;

        Roaming roaming = (Roaming) o;

        if (getHomeMcc() != roaming.getHomeMcc()) return false;
        return getServingMcc() == roaming.getServingMcc();

    }

    @Override
    public int hashCode() {
        int result = getHomeMcc();
        result = 1099 * result + getServingMcc();
        return result;
    }
}
