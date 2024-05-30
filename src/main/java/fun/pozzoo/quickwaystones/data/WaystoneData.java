package fun.pozzoo.quickwaystones.data;

import fun.pozzoo.quickwaystones.QuickWaystones;
import org.bukkit.Location;

public class WaystoneData {
    private int id;
    private String name;
    private String owner;
    private Location location;

    public WaystoneData(Location location, String owner) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        name = "Waystone " + id ;
        this.location = location;
        this.owner = owner;
    }
    public WaystoneData(String name, Location location, String owner) {
        id = QuickWaystones.getAndIncrementLastWaystoneID();
        this.name = name;
        this.location = location;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
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
