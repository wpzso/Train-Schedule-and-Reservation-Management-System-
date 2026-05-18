package models;

public class Schedule {
    private int id;
    private int trainId;
    private String routeName;
    private String departureTime;

    // Enriched fields (joined from Trains table for display)
    private String trainName;
    private int totalCapacity;

    // ── Fields added to fix compilation errors ────────────────────────────
    private String fromLocation;
    private String toLocation;
    private double price;

    public Schedule(int id, int trainId, String routeName, String departureTime) {
        this.id = id;
        this.trainId = trainId;
        this.routeName = routeName;
        this.departureTime = departureTime;
    }

    // Getters
    public int getId()               { return id; }
    public int getTrainId()          { return trainId; }
    public String getRouteName()     { return routeName; }
    public String getDepartureTime() { return departureTime; }
    public String getTrainName()     { return trainName; }
    public int getTotalCapacity()    { return totalCapacity; }
    public String getFromLocation()  { return fromLocation; }
    public String getToLocation()    { return toLocation; }
    public double getPrice()         { return price; }

    // Setters
    public void setId(int id)                          { this.id = id; }
    public void setTrainId(int trainId)                { this.trainId = trainId; }
    public void setRouteName(String routeName)         { this.routeName = routeName; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setTrainName(String trainName)         { this.trainName = trainName; }
    public void setTotalCapacity(int totalCapacity)    { this.totalCapacity = totalCapacity; }
    public void setFromLocation(String fromLocation)   { this.fromLocation = fromLocation; }
    public void setToLocation(String toLocation)       { this.toLocation = toLocation; }
    public void setPrice(double price)                 { this.price = price; }

    @Override
    public String toString() {
        return "[" + id + "] " + routeName + " | " + departureTime;
    }
}