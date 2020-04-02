package com.terminal29;

import javafx.util.Pair;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class BalloonEntityContainer {
    private static int _travelDistance = 100;
    private static float _speed = 0.05f;
    static Random r = new Random();
    static boolean _hasRegisteredHandler = false;
    static int _maxAliveTicks = 5*60*20; // auto remove after 5 minutes

    private double _randomAngle;
    private int _aliveTicks;
    private Plugin _plugin;
    private Entity _balloonBaseEntity;
    private Location _centreLocation;
    private Location _startLocation;
    private Location _endLocation;
    private World _world;
    private List<Pair<ItemStack, Pair<Integer, Integer>>> _drops;

    public BalloonEntityContainer(Plugin plugin, Location centreLocation, World world, List<Pair<ItemStack, Pair<Integer, Integer>>> drops){
        _plugin = plugin;
        _centreLocation = centreLocation;
        _world = world;
        _randomAngle = r.nextInt(360);
        _aliveTicks = 0;
        _drops = drops;

        if(!_hasRegisteredHandler){
            _hasRegisteredHandler = true;
            _plugin.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onEntityDeath(EntityDeathEvent event){
                    if(event.getEntityType() == EntityType.SHEEP){
                        Sheep entity = (Sheep)event.getEntity();
                        List<MetadataValue> meta = entity.getMetadata("isBalloon");
                        if (!meta.isEmpty() && meta.get(0).asBoolean()) {

                            final int floorY = _world.getHighestBlockYAt(entity.getLocation());
                            final Location loc = entity.getLocation();
                            int fireworkCount = 5;
                            int fireworkTickDelay = 4;
                            for(int i = 0; i < fireworkCount; i++){
                                final int i2 = i;
                                _plugin.getServer().getScheduler().runTaskLater(_plugin, () -> {

                                    Location fireworkLocation = loc.clone();

                                    fireworkLocation.setY((loc.getY() - (loc.getY()-floorY)*(i2/(double)fireworkCount)));

                                    Firework fw = (Firework) _world.spawnEntity(fireworkLocation, EntityType.FIREWORK);
                                    FireworkMeta fwm = fw.getFireworkMeta();

                                    fwm.setPower(2);
                                    fwm.addEffect(FireworkEffect.builder().withColor(Color.RED).flicker(true).build());
                                    fwm.addEffect(FireworkEffect.builder().withColor(Color.WHITE).flicker(true).build());

                                    fw.setFireworkMeta(fwm);
                                    fw.detonate();
                                }, fireworkTickDelay*i);
                            }
                            _plugin.getServer().getScheduler().runTaskLater(_plugin, () -> {
                                Location spawnLocation = loc.clone();
                                spawnLocation.setY(floorY+1);

                                Pair<ItemStack, Pair<Integer, Integer>> stack = _drops.get(r.nextInt(_drops.size()));
                                ItemStack dropStack = stack.getKey().clone();
                                int min = stack.getValue().getKey();
                                int max = stack.getValue().getValue();
                                dropStack.setAmount(min+r.nextInt(max - min));

                                _world.dropItem(spawnLocation, dropStack);
                            }, (fireworkCount+1)*(fireworkTickDelay));
                        }
                    }
                }

            }, _plugin);
        }

    }

    private Entity spawnBalloon(){
        _startLocation = _centreLocation.clone().subtract((_travelDistance*Math.cos(Math.toRadians(_randomAngle)))/2, 0, (_travelDistance*Math.sin(Math.toRadians(_randomAngle)))/2);
        _endLocation = _centreLocation.clone().add((_travelDistance*Math.cos(Math.toRadians(_randomAngle)))/2, 0, (_travelDistance*Math.sin(Math.toRadians(_randomAngle)))/2);
        _startLocation.setY(120);
        _endLocation.setY(120);

        Entity balloonBase = _world.spawnEntity(_startLocation, EntityType.SHEEP);
        Sheep sheepEntity = (Sheep)balloonBase;
        sheepEntity.setMetadata("isBalloon",new FixedMetadataValue(_plugin, true));
        sheepEntity.setColor(DyeColor.RED);
        sheepEntity.setLootTable(LootTables.EMPTY.getLootTable());
        sheepEntity.setHealth(0.5f);
        sheepEntity.setGravity(false);

        return balloonBase;
    }

    public void remove(){
        if(_balloonBaseEntity != null){
            _balloonBaseEntity.remove();
        }
    }

    public boolean runTick(){
        _aliveTicks++;
        if(_balloonBaseEntity == null){
            _balloonBaseEntity = spawnBalloon();
            return true;
        }
        if(!_balloonBaseEntity.isValid()){
            return false;
        }else if(_aliveTicks > _maxAliveTicks){
            remove();
            return false;
        }else{
            Vector currentPos = _balloonBaseEntity.getLocation().toVector();
            Vector distanceLeft = _endLocation.clone().subtract(currentPos).toVector();
            if(distanceLeft.length() == 10){ // We are close enough to the end position
                remove();
                return false;
            }else{
                distanceLeft.normalize();
                _balloonBaseEntity.setVelocity(distanceLeft.multiply(_speed));
            }
        }
        return true;
    }
}
