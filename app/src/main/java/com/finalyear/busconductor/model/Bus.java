package com.finalyear.busconductor.model;

public class Bus {
    private String BusNumber;
    private String BusSource;
    private String BusDestination;

    public Bus(String busNumber, String busSource, String busDestination) {
        BusNumber = busNumber;
        BusSource = busSource;
        BusDestination = busDestination;
    }

    public Bus() {
    }

    public String getBusNumber() {
        return BusNumber;
    }

    public void setBusNumber(String busNumber) {
        BusNumber = busNumber;
    }

    public String getBusSource() {
        return BusSource;
    }

    public void setBusSource(String busSource) {
        BusSource = busSource;
    }

    public String getBusDestination() {
        return BusDestination;
    }

    public void setBusDestination(String busDestination) {
        BusDestination = busDestination;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "BusNumber='" + BusNumber + '\'' +
                ", BusSource='" + BusSource + '\'' +
                ", BusDestination='" + BusDestination + '\'' +
                '}';
    }
}
