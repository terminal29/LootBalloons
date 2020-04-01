package com.terminal29;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    IPlayerOperator _callback;

    PlayerLeaveListener(IPlayerOperator callback){
        _callback = callback;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event)
    {
        _callback.call(event.getPlayer());
    }
}
