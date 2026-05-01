/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

/**
 *
 * @author cmber
 */

import exception.RoomNotEmptyException;
import exception.RoomNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import models.Room;

@Path("/rooms")
public class RoomResource {

    // Static map 
    static Map<String, Room> rooms = new HashMap<>();

    // Static block 
    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("LAB-101", "Computer Lab", 30);
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
    }

    // GET all rooms 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
    
    // To test catch all exception
    @GET
    @Path("/crash")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerCrash() {
       
        String nullString = null;
        nullString.length(); 
        return Response.ok().build(); 
    }

    // GET by ID 
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new RoomNotFoundException("Room with ID " + roomId + " not found.");
        }
        return room;
    }

    // POST 
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        rooms.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // DELETE 
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        
        
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found").build();
        }

        
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still has sensors assigned to it.");
        }
        
       
        rooms.remove(roomId);
        return Response.status(Response.Status.OK)
                .entity("Room deleted").build();
    }
}