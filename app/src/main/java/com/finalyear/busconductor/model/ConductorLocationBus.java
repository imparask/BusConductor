package com.finalyear.busconductor.model;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class ConductorLocationBus {

    private GeoPoint geoPoint;
    private Conductor conductor;
    private @ServerTimestamp
    Timestamp timestamp;
    private String busNumber;
    private String busSource;
    private String busDestination;
    private String busCount;

    public ConductorLocationBus() {
    }

    public ConductorLocationBus(GeoPoint geoPoint, Conductor conductor, Timestamp timestamp, String busNumber, String busSource, String busDestination,String busCount) {
        this.geoPoint = geoPoint;
        this.conductor = conductor;
        this.timestamp = timestamp;
        this.busNumber = busNumber;
        this.busSource = busSource;
        this.busDestination = busDestination;
        this.busCount = busCount;
    }

    public String getBusCount() {
        return busCount;
    }

    public void setBusCount(String busCount) {
        this.busCount = busCount;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusSource() {
        return busSource;
    }

    public void setBusSource(String busSource) {
        this.busSource = busSource;
    }

    public String getBusDestination() {
        return busDestination;
    }

    public void setBusDestination(String busDestination) {
        this.busDestination = busDestination;
    }

    @Override
    public String toString() {
        return "ConductorLocationBus{" +
                ", busNumber='" + busNumber + '\'' +
                ", busSource='" + busSource + '\'' +
                ", busDestination='" + busDestination + '\'' +
                '}';
    }
}
