package com.terminal29;

import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.ref.WeakReference;
import java.util.*;

public final class LootBalloons extends JavaPlugin {

    ArrayList<Player> _connectedPlayers = null;

    ArrayList<BalloonEntityContainer> _spawnedBalloons;

    List<Pair<ItemStack, Pair<Integer, Integer>>> _balloonLoot;

    double _spawnChance = 0.5f; // 50% chance to spawn

    int _spawnInterval = 20*60*2; // Every 2 minutes

    Random r = new Random();

    private List<Pair<ItemStack, Pair<Integer, Integer>>> loadBalloonLoot(){
        List<Pair<ItemStack, Pair<Integer, Integer>>> loot = new ArrayList<>();
        ConfigurationSection lootSection = getConfig().getConfigurationSection("loot");
        for(String key : lootSection.getKeys(false)){
            ConfigurationSection thisItem = lootSection.getConfigurationSection(key);
            int min = thisItem.getInt("min");
            int max = thisItem.getInt("max");
            if(max < min){
                getLogger().warning(String.format("min is greater than max for %s, skipping...", key));
                continue;
            }
            Material m = Material.matchMaterial(key);
            if(m == null){
                getLogger().warning(String.format("%s is not a material name, skipping...", key));
                continue;
            }
            loot.add(new Pair<>(new ItemStack(m), new Pair<>(min,max)));

        }
        return loot;
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        _spawnChance = getConfig().getDouble("spawnChance");

        _spawnInterval = getConfig().getInt("spawnInterval");

        getLogger().info(String.format("Starting LootBalloons with %f chance at %d tick intervals", _spawnChance, _spawnInterval));

        _balloonLoot = loadBalloonLoot();

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
                    _spawnedBalloons.add(new BalloonEntityContainer(this, location, player.getWorld(), _balloonLoot));
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
                _spawnedBalloons.removeIf(entity->!entity.runTick());
            }
        }, 0, 1);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for(Player player : _connectedPlayers){
                    if(r.nextDouble() <= _spawnChance)
                        _spawnedBalloons.add(new BalloonEntityContainer(LootBalloons.this, player.getLocation(), player.getWorld(), _balloonLoot));
                }
            }
        }, _spawnInterval, _spawnInterval); // Spawn one every 2 minutes

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        _spawnedBalloons.forEach(BalloonEntityContainer::remove);
    }
}
