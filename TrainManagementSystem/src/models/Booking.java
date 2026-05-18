package models;

public class Booking {
    private int id;
    private int scheduleId;
    private String passengerName;
    private String passengerEmail;
    private int seatNumber;

    // Enriched fields for display
    private String routeName;
    private String departureTime;
    private String trainName;
    private String fromLocation;
    private String toLocation;
    private double price;

    public Booking(int id, int scheduleId, String passengerName, int seatNumber) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
    }

    // Getters
    public int getId()                  { return id; }
    public int getScheduleId()          { return scheduleId; }
    public String getPassengerName()    { return passengerName; }
    public String getPassengerEmail()   { return passengerEmail; }
    public int getSeatNumber()          { return seatNumber; }
    public String getRouteName()        { return routeName; }
    public String getDepartureTime()    { return departureTime; }
    public String getTrainName()        { return trainName; }
    public String getFromLocation()     { return fromLocation; }
    public String getToLocation()       { return toLocation; }
    public double getPrice()            { return price; }

    // Setters
    public void setId(int id)                          { this.id = id; }
    public void setScheduleId(int scheduleId)          { this.scheduleId = scheduleId; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public void setPassengerEmail(String email)        { this.passengerEmail = email; }
    public void setSeatNumber(int seatNumber)          { this.seatNumber = seatNumber; }
    public void setRouteName(String routeName)         { this.routeName = routeName; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setTrainName(String trainName)         { this.trainName = trainName; }
    public void setFromLocation(String fromLocation)   { this.fromLocation = fromLocation; }
    public void setToLocation(String toLocation)       { this.toLocation = toLocation; }
    public void setPrice(double price)                 { this.price = price; }
}