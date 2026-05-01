/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus;

/**
 *
 * @author cmber
 */
import com.mycompany.smartcampus.resources.DiscoveryResource;
import com.mycompany.smartcampus.resources.RoomResource;
import com.mycompany.smartcampus.resources.SensorReadingResource;
import com.mycompany.smartcampus.resources.SensorResource;
import exception.GlobalExceptionMapper;
import exception.LinkedResourceNotFoundExceptionMapper;
import exception.RoomNotEmptyExceptionMapper;
import exception.RoomNotFoundExceptionMapper;
import exception.SensorUnavailableExceptionMapper;
import filter.LoggingFilter;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(SensorReadingResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(LoggingFilter.class);
        classes.add(RoomNotFoundExceptionMapper.class);
        return classes;
    }
}