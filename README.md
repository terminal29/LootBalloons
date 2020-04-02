# LootBalloons
A Spigot/Paper plugin to add Animal Crossing loot balloons to Minecraft

# Summary

This is a plugin for Spigot/Paper that randomly summons floating sheep (balloons) that when shot down drop loot. Tested on Paper 1.15.2 #146.


# Permissions
```yaml
lootballoons.spawn # Ability to run the command /lootballoons spawn 
lootballoons.randomspawn # If balloons are allowed to randomly spawn near your location
```

# Config
```yaml
spawnInterval: 20 # Ticks between when a loot balloon will attempt to spawn
spawnChance: 1.0 # Chance a loot balloon will spawn every time spawnInterval ticks over (0.0 no chance, 1.0 always)
loot: # Available loot to spawn
  "minecraft:diamond":
    max: 4
    min: 1
  "minecraft:coal":
    max: 16
    min: 8
  "minecraft:iron_ingot":
    max: 8
    min: 2
  "minecraft:gold_ingot":
    max: 4
    min: 2
```
