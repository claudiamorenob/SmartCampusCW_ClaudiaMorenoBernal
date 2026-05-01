/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

/**
 *
 * @author cmber
 */

import exception.SensorUnavailableException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import models.SensorReading;

public class SensorReadingResource {

    private String sensorId;

    // Constructor
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET all readings
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getReadings() {
        List<SensorReading> sensorReadings = SensorResource.readings.get(sensorId);
        
        if (sensorReadings == null) {
            return new ArrayList<>();
        }
        return sensorReadings;
    }

    // POST  
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Check sensor exists
        if (!SensorResource.sensors.containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found").build();
        }
        // Check sensor is not under maintenance
        if (SensorResource.sensors.get(sensorId).getStatus().equals("MAINTENANCE")) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is under maintenance and cannot accept readings.");
        }

        // Create new reading 
        SensorReading newReading = new SensorReading(reading.getValue());

        // Add to readings list
        SensorResource.readings.get(sensorId).add(newReading);

        // Update sensor's currentValue 
        SensorResource.sensors.get(sensorId)
                .setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                .entity(newReading).build();
    }
}