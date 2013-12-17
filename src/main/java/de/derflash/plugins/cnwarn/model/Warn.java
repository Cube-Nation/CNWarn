package de.derflash.plugins.cnwarn.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "cn_warns")
public class Warn {
    @Id
    private int id;

    @NotNull
    private String playername;

    @NotNull
    private String staffname;

    @Column
    private String message;

    @Column
    private int rating;

    @NotNull
    private Date created;

    @Column
    private Date accepted;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String ply) {
        this.playername = ply;
    }

    public Player getPlayer() {
        return Bukkit.getServer().getPlayer(playername);
    }

    public void setPlayer(Player player) {
        this.playername = player.getName();
    }

    public String getStaffname() {
        return staffname;
    }

    public void setStaffname(String staffname) {
        this.staffname = staffname;
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