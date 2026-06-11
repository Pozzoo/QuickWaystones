package dev.pozzoo.quickwaystones.data;

import dev.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Location;

import java.util.UUID;

public class WaystoneData {
    private final int id;
    private String name;
    private final UUID owner;
    private final Location location;
    private int direction = 0;

    public WaystoneData(Location location, UUID owner) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        name = "Waystone " + id ;
        this.location = location;
        this.owner = owner;
    }
    public WaystoneData(Integer id, String name, Location location, UUID owner, int direction) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.owner = owner;
        this.direction = direction;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "WaystoneData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", owner='" + owner + '\'' +
                ", direction=" + direction +
                '}';
    }
}
