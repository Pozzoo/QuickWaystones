package fun.pozzoo.quickwaystones.data;

import fun.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Location;

import java.util.UUID;

public class WaystoneData {
    private final int id;
    private String name;
    private final UUID owner;
    private final Location location;

    public WaystoneData(Location location, UUID owner) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        name = "Waystone " + id ;
        this.location = location;
        this.owner = owner;
    }
    public WaystoneData(String name, Location location, UUID owner) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        this.name = name;
        this.location = location;
        this.owner = owner;
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

    @Override
    public String toString() {
        return "WaystoneData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", owner='" + owner + '\'' +
                '}';
    }
}
