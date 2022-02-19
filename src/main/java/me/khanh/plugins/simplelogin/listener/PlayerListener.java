package me.khanh.plugins.simplelogin.listener;

import me.khanh.plugins.simplelogin.SimpleLogin;
import me.khanh.plugins.simplelogin.storage.MySQL;
import me.khanh.plugins.simplelogin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.geysermc.cumulus.CustomForm;
import org.geysermc.cumulus.impl.CustomFormImpl;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class PlayerListener implements Listener, Utils {

    private final SimpleLogin plugin = SimpleLogin.getInstance();
    private final MySQL mySQL = plugin.getMySQL();
    private static final Set<Player> loginPlayers = new HashSet<>();
    private static final Set<Player> registerPlayer = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event){
        String name = event.getPlayer().getName().toLowerCase();
        mySQL.connect();
        if (!mySQL.exists(name)){
            registerPlayer.add(event.getPlayer());
            if (!FloodgateApi.getInstance().isFloodgateId(event.getPlayer().getUniqueId())){
                openRegisterAnvil(event.getPlayer());
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId())
                        .sendForm(getRegisterBuilder(event.getPlayer())), 20L);
            }

        } else {
            loginPlayers.add(event.getPlayer());
            if (!FloodgateApi.getInstance().isFloodgateId(event.getPlayer().getUniqueId())){
                openLoginAnvil(event.getPlayer());
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId())
                        .sendForm(getLoginFormBuilder(event.getPlayer(), false)), 20L);
            }
        }
    }
    public void openLoginAnvil(Player player){
        AnvilGUI.Builder builder = new AnvilGUI.Builder();
        builder.title(getConfigString("java.login.title"))
                .text(getConfigString("java.login.text"))
                .plugin(plugin)
                .onComplete((anvil_player, s) -> {
                    if (plugin.getConfig().getBoolean("java.login.exit.enable")){
                        if (plugin.getConfig().getBoolean("java.login.exit.ignore_case")){
                            if (s.equalsIgnoreCase(getConfigString("java.login.exit.text"))){
                                return AnvilGUI.Response.close();
                            }
                        } else {
                            if (s.equals(getConfigString("java.login.exit.text"))){
                                return AnvilGUI.Response.close();
                            }
                        }
                    }

                    if (s.equals(mySQL.getPassword(anvil_player.getName().toLowerCase()))){
                        anvil_player.sendMessage(getConfigString("message.login_success"));
                        return AnvilGUI.Response.close();
                    } else {
                        if (plugin.getConfig().getBoolean("java.login.kick")){
                            anvil_player.kickPlayer(getConfigString("java.login.wrong_pass"));
                            return AnvilGUI.Response.close();
                        } else {
                            return AnvilGUI.Response.text(getConfigString("java.login.wrong_pass"));
                        }
                    }
                });
        if (plugin.getConfig().getBoolean("java.login.prevent_close")){
            builder.preventClose();
        }
        builder.open(player);
    }

    public CustomForm.Builder getRegisterBuilder(Player player){
        CustomForm.Builder builder = new CustomFormImpl.Builder();
        builder.title(getConfigString("bedrock.register.title"))
                .input(getConfigString("bedrock.register.text"), getConfigString("bedrock.register.text_in_" +
                        "box"));

        for (String s: plugin.getConfig().getStringList("bedrock.register.contents")){
            builder.label(s);
        }
        builder.responseHandler(((customForm, s) -> {
            CustomFormResponse response = customForm.parseResponse(s);
            if (!response.isCorrect()){
                FloodgateApi.getInstance().getPlayer(player.getUniqueId()).sendForm(getRegisterBuilder(player));
                return;
            }

            String input = response.getInput(0);
            if (input == null){
                input = "";
            }
            if (plugin.getConfig().getBoolean("bedrock.register.exit.enable")){
                if (plugin.getConfig().getBoolean("bedrock.register.exit.ignore_case")){
                    if (input.equalsIgnoreCase(getConfigString("bedrock.register.exit.text"))){
                        return;
                    }
                } else {
                    if (input.equals(getConfigString("bedrock.register.exit.text"))){
                        return;
                    }
                }
            }
            mySQL.setPassword(player.getName().toLowerCase(), input);
            player.sendMessage(getConfigString("message.register_success"));
            getRegisterPlayer().remove(player);
        }));
        return builder;
    }

    public CustomForm.Builder getLoginFormBuilder(Player player, boolean wrong_password){
        CustomForm.Builder builder = new CustomFormImpl.Builder();
        builder.title(getConfigString("bedrock.login.title"))
                .input(getConfigString("bedrock.login.text"), getConfigString("bedrock.login.text_in_" +
                        "box"));

        for (String s: plugin.getConfig().getStringList("bedrock.login.contents")){
            builder.label(s);
        }
        if (wrong_password){
            builder.label(getConfigString("bedrock.login.wrong_pass"));
        }

        builder.responseHandler(((customForm, s) -> {
            CustomFormResponse response = customForm.parseResponse(s);
            if (!response.isCorrect()){
                if (plugin.getConfig().getBoolean("bedrock.register.prevent_close")){
                    FloodgateApi.getInstance().getPlayer(player.getUniqueId()).sendForm(getRegisterBuilder(player));
                }
                return;
            }

            String input = response.getInput(0);
            if (input == null){
                input = "";
            }
            if (plugin.getConfig().getBoolean("bedrock.login.exit.enable")){
                if (plugin.getConfig().getBoolean("bedrock.login.exit.ignore_case")){
                    if (input.equalsIgnoreCase(getConfigString("bedrock.login.exit.text"))){
                        return;
                    }
                } else {
                    if (input.equals(getConfigString("bedrock.login.exit.text"))){
                        return;
                    }
                }
            }

            if (input.equals(mySQL.getPassword(player.getName().toLowerCase()))){
                player.sendMessage(getConfigString("message.login_success"));
                getLoginPlayers().remove(player);
            } else {
                if (plugin.getConfig().getBoolean("bedrock.login.kick")){
                    player.kickPlayer(getConfigString("bedrock.login.wrong_pass"));
                    getLoginPlayers().remove(player);
                } else {
                    FloodgateApi.getInstance().getPlayer(player.getUniqueId()).sendForm(getLoginFormBuilder(player, true));
                }
            }
        }));
        return builder;
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
                    getRegisterPlayer().remove(anvil_player);
                    return AnvilGUI.Response.close();
                });
        if (plugin.getConfig().getBoolean("java.register.prevent_close")){
            builder.preventClose();
        }
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event){
        if (getRegisterPlayer().contains(event.getPlayer()) || getLoginPlayers().contains(event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event){
        if (getRegisterPlayer().contains(event.getPlayer()) || getLoginPlayers().contains(event.getPlayer())){
            event.getPlayer().sendMessage(getConfigString("message.no_chat"));
            event.setCancelled(true);
        }
    }
}
