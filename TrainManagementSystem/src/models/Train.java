package models;

public class Train {
    private int id;
    private String trainName;
    private int totalCapacity;

    public Train(int id, String trainName, int totalCapacity) {
        this.id = id;
        this.trainName = trainName;
        this.totalCapacity = totalCapacity;
    }

    // Getters
    public int getId()             { return id; }
    public String getTrainName()   { return trainName; }
    public int getTotalCapacity()  { return totalCapacity; }

    // Setters
    public void setId(int id)                      { this.id = id; }
    public void setTrainName(String trainName)      { this.trainName = trainName; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    @Override
    public String toString() {
        return "[" + id + "] " + trainName + " (Cap: " + totalCapacity + ")";
    }
}