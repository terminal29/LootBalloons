package com.terminal29;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    IPlayerOperator _callback;

    PlayerJoinListener(IPlayerOperator callback){
        _callback = callback;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        _callback.call(event.getPlayer());
    }
}
