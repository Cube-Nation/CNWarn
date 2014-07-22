package de.cubenation.plugins.cnwarn.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.avaje.ebean.validation.NotNull;

@Entity
@Table(name = "cn_warns")
public class Warn {
    @Id
    private int id;

    @Column(name = "playername", nullable = false)
    private String playerName;

    @Column(name = "staffname", nullable = false)
    private String staffName;

    @Column
    private String message;

    @Column
    private int rating;

    @NotNull
    private Date created;

    @Column
    private Date accepted;

    @Column
    private String world;
    
    @Column(name = "location_x")
    private int location_x;
    
    @Column(name = "location_y")
    private int location_y;

	@Column(name = "location_z")
    private int location_z;
    

    public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public int getLocation_x() {
		return location_x;
	}

	public void setLocation_x(int location_x) {
		this.location_x = location_x;
	}

	public int getLocation_y() {
		return location_y;
	}

	public void setLocation_y(int location_y) {
		this.location_y = location_y;
	}

	public int getLocation_z() {
		return location_z;
	}

	public void setLocation_z(int location_z) {
		this.location_z = location_z;
	}

    public void setLocation(Location loc) {
    	this.world = loc.getWorld().getName();
    	this.location_x = loc.getBlockX();
    	this.location_y = loc.getBlockY();
    	this.location_z = loc.getBlockZ();
    }
    
    public Location getLocation() {
    	if (this.world == null) return null;
    	
    	return new Location(Bukkit.getWorld(this.world), location_x, location_y, location_z);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getAccepted() {
        return accepted;
    }

    public void setAccepted(Date accepted) {
        this.accepted = accepted;
    }
}