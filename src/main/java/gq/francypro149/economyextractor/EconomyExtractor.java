package gq.francypro149.economyextractor;

import me.antonschouten.economy.API.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.kiyoshi.hexaecon.api.HexaEconAPI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class EconomyExtractor extends JavaPlugin implements Listener {
    private File folder;
    private File file;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("EcoSystem") == null) {
            log(new LogRecord(Level.SEVERE, "EcoSystem not found, the plugin was disabled"), "EcoSystemDisable");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (getServer().getPluginManager().getPlugin("HexaEcon") == null) {
            log(new LogRecord(Level.SEVERE, "HexaEcon not found, the plugin was disabled"), "EcoSystemDisable");
            getServer().getPluginManager().disablePlugin(this);
        }

        folder = new File(getDataFolder(), "extracted");
        file = new File(getDataFolder(), "extracted/data.csv");

        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws SQLException {
        Player p = e.getPlayer();
        String playerName = p.getName();
        int balance = Economy.getBal(p);

        if (balance > 0) {
            if (!isExtractedPlayerInList(playerName)) {
                HexaEconAPI.deleteBankAccount(p);
                HexaEconAPI.createBankAccount(p, (long) balance);
                Economy.removeBal(p, balance); //Or Economy.resetBal(p);
                addExtractedPlayer(playerName);
                log(new LogRecord(Level.INFO, "[Economy++Extractor] Successfully converted bank account for "+playerName+" - "+(long) balance), "Economy++Extractor");
            }
        }
    }

    private void addExtractedPlayer(String playerName) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file, true));
            writer.print(playerName);
            writer.print(",");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isExtractedPlayerInList(String playerName) {
        try {
            for (String line : java.nio.file.Files.readAllLines(file.toPath())) {
                String[] colums = line.split(",");
                for (String column : colums) {
                    if (column.equals(playerName)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String colorize(Level level, String message) {
        String color;
        switch (level.getName()) {
            case "SEVERE":
                color = "\u001B[31m";
                break;
            case "WARNING":
                color = "\u001B[33m";
                break;
            case "INFO":
                color = "\u001B[1;34m";
            case "CONFIG":
            case "FINE":
            case "FINER":
            case "FINEST":
                color = "\u001B[32m";
                break;
            default:
                color = "\u001B[0m";
                break;
        }

        return color+message+"\u001B[0m";
    }

    private void log(LogRecord record, String name) {
        if (record != null && Logger.getLogger(name).isLoggable(record.getLevel())) {
            String message = colorize(record.getLevel(), record.getMessage());
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }
}
