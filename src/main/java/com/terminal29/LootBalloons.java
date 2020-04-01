package com.terminal29;

import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public final class LootBalloons extends JavaPlugin {

    static ArrayList<Player> _connectedPlayers = null;

    static ArrayList<BalloonEntityContainer> _spawnedBalloons;

    @Override
    public void onEnable() {
        // Get current players
        _connectedPlayers = new ArrayList<>(getServer().getOnlinePlayers());

        _spawnedBalloons = new ArrayList<>();

        // Keep track of additional players when they join
        getServer().getPluginManager().registerEvents(new PlayerJoinListener((player)->{
            _connectedPlayers.add(player);
        }), this);

        // Stop watching players when they disconnect
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener((player)->{
            _connectedPlayers.add(player);
        }), this);

        this.getCommand("lootballoons").setExecutor((sender, command, label, args) -> {
            if(args.length == 1 && args[0].equalsIgnoreCase("spawn")){
                if(sender instanceof Player){
                    Player player = ((Player)sender);
                    Location location = player.getLocation();
                    _spawnedBalloons.add(new BalloonEntityContainer(this, location, player.getWorld()));
                    sender.sendMessage(String.format("Spawning loot balloon at %d %d %d", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                }else{
                    sender.sendMessage("Cannot spawn from command line");
                }
            }
            return true;
        });

        // register tick event handler to update positions of balloons
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                // run runTick on all balloons and remove any containers that ask to be removed (their base entity was destroyed or something)
                _spawnedBalloons.removeIf(entity->{
                    boolean keep = entity.runTick();
                    if(!keep){
                        System.out.println("Removing balloon entity container");
                    }
                    return !keep;
                });


            }
        }, 0, 1);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : _connectedPlayers){
                    _spawnedBalloons.add(new BalloonEntityContainer(LootBalloons.this, player.getLocation(), player.getWorld()));
                }
            }
        }, 20*60*2, 20*60*2); // Spawn one every 2 minutes

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        _spawnedBalloons.forEach(BalloonEntityContainer::remove);
    }
}
