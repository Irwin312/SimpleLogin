package me.khanh.plugins.simplelogin.utils;

import me.khanh.plugins.simplelogin.SimpleLogin;
import org.bukkit.ChatColor;

import java.util.Objects;

public interface Utils {
    default String getConfigString(String path){
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(SimpleLogin.getInstance().getConfig().getString(path)));
    }

    default String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    default void error(String s){
        SimpleLogin.getInstance().getServer().getLogger().severe(color("&7[&bSimpleLogin&7] &cERROR: &c" + s));
    }

    default void info(String s){
        SimpleLogin.getInstance().getServer().getLogger().info(color("&7[&bSimpleLogin&7] &f" + s));
    }
}
