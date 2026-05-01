/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

/**
 *
 * @author cmber
 */

import exception.LinkedResourceNotFoundException;
import exception.RoomNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import models.Sensor;
import models.SensorReading;

@Path("/sensors")
public class SensorResource {

    // Static maps
    public static Map<String, Sensor> sensors = new HashMap<>();
    public static Map<String, List<SensorReading>> readings = new HashMap<>();

    // Static block
    static {
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        
        // Link sensors to rooms
        RoomResource.rooms.get("LIB-301").getSensorIds().add("TEMP-001");
        RoomResource.rooms.get("LAB-101").getSensorIds().add("CO2-001");
        
        // Initialise empty reading lists
        readings.put("TEMP-001", new ArrayList<>());
        readings.put("CO2-001", new ArrayList<>());
    }

    // GET all sensors
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(sensors.values());
        
        if (type != null) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : allSensors) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return filtered;
        }
        return allSensors;
    }

    // GET by ID
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            throw new RoomNotFoundException("Sensor with ID " + sensorId + " not found.");
        }
        return sensor;
    }

    // POST
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSensor(Sensor sensor) {
        if (!RoomResource.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " not found.");
        }
        
        sensors.put(sensor.getId(), sensor);
        
        // Link sensor to room
        RoomResource.rooms.get(sensor.getRoomId())
                .getSensorIds().add(sensor.getId());
        
        // Initialise empty readings list for new sensor
        readings.put(sensor.getId(), new ArrayList<>());
        
        return Response.status(Response.Status.CREATED)
                .entity(sensor).build();
    }

    // Sub-resource locator
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}