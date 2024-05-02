package com.github.pozzoo.quickwaystones.data;

import com.github.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Location;

public class WaystoneData {
    private int id;
    private String name;
    private Location location;

    public WaystoneData(Location location) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        name = "Waystone " + id ;
        this.location = location;
    }
    public WaystoneData(String name, Location location) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "WaystoneData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                '}';
    }
}
