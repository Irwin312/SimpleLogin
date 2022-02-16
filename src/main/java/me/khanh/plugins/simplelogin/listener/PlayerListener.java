package me.khanh.plugins.simplelogin.listener;

import me.khanh.plugins.simplelogin.SimpleLogin;
import me.khanh.plugins.simplelogin.storage.MySQL;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerListener implements Listener {

    private final SimpleLogin plugin = SimpleLogin.getInstance();
    private final MySQL mySQL = plugin.getMySQL();
    private static final Set<Player> loginPlayers = new HashSet<>();
    private static final Set<Player> registerPlayer = new HashSet<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        String name = event.getPlayer().getName().toLowerCase();
        if (!mySQL.exists(name)){
            mySQL.createPlayer(name);
        }
        if (!mySQL.registered(name)){
            registerPlayer.add(event.getPlayer());
        } else {
            loginPlayers.add(event.getPlayer());
        }
        mySQL.disconnect();
    }

    public Set<Player> getLoginPlayers() {
        return loginPlayers;
    }

    public Set<Player> getRegisterPlayer() {
        return registerPlayer;
    }
}
