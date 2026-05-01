/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author cmber
 */
import java.util.UUID;

public class SensorReading {
    private String id;
    private long timestamp;
    private double value;

    // Default constructor
    public SensorReading() {}

    // Constructor
    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString(); 
        this.timestamp = System.currentTimeMillis(); 
        this.value = value;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
