package me.khanh.plugins.simplelogin.commands;

import me.khanh.plugins.simplelogin.SimpleLogin;
import me.khanh.plugins.simplelogin.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SimpleLoginCommand implements CommandExecutor, Utils {
    private final SimpleLogin plugin = SimpleLogin.getInstance();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args != null && args[0].equalsIgnoreCase("reload")){
            if (sender.hasPermission("simplelogin.reload")){
                plugin.reload();
            } else {
                sender.sendMessage(getConfigString("message.no_permission"));
            }
        }
        return false;
    }
}
