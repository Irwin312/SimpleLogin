package me.khanh.plugins.simplelogin.listener;

import me.khanh.plugins.simplelogin.SimpleLogin;
import me.khanh.plugins.simplelogin.storage.MySQL;
import me.khanh.plugins.simplelogin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class PlayerListener implements Listener, Utils {

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
            openRegisterAnvil(event.getPlayer());
        } else {
            loginPlayers.add(event.getPlayer());
        }
    }

    public void openRegisterAnvil(Player player){
        AnvilGUI.Builder builder = new AnvilGUI.Builder();
        builder.title(getConfigString("java.register.title"))
                .text(getConfigString("java.register.text"))
                .plugin(plugin)
                .onComplete((anvil_player, s) -> {
                    if (plugin.getConfig().getBoolean("java.register.exit.enable")){
                        if (plugin.getConfig().getBoolean("java.register.exit.ignore_case")){
                            if (s.equalsIgnoreCase(getConfigString("java.register.exit.text"))){
                                return AnvilGUI.Response.close();
                            }
                        } else {
                            if (s.equals(getConfigString("java.register.exit.text"))){
                                return AnvilGUI.Response.close();
                            }
                        }
                    }
                    mySQL.setPassword(anvil_player.getName().toLowerCase(), s);
                    return AnvilGUI.Response.close();
                });
        builder.open(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        if (getLoginPlayers().contains(event.getPlayer())){
            loginPlayers.clear();
        }
        if (getRegisterPlayer().contains(event.getPlayer())){
            registerPlayer.clear();
        }
    }

    public Set<Player> getLoginPlayers() {
        return loginPlayers;
    }

    public Set<Player> getRegisterPlayer() {
        return registerPlayer;
    }
}
