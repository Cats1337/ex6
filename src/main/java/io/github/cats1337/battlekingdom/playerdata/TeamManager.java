package io.github.cats1337.battlekingdom.playerdata;

import io.github.cats1337.battlekingdom.BattleKingdom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.marcusslover.plus.lib.text.Text;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;


public class TeamManager {
    protected static final BattleKingdom plugin = BattleKingdom.getInstance();
    static FileConfiguration config = BattleKingdom.getInstance().getConfig();
    protected final static String dataFolder = plugin.getDataFolder().getAbsolutePath(); // plugins/BattleKingdom

    public static void assignAllRandomTeam(Player p) {
        if (p == null || !p.isOnline()) {
            return;
        }
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        // Clear team leader status if the player was a team leader
        if (serverPlayer.isTeamLeader()) {
            serverPlayer.setTeamLeader(false);
        }

        // Ensure that scoreboard teams exist
        scoreboardTeams();
        // get players location

        // Get all teams and shuffle them
        List<String> teams = getLiteralTeamNames();
        Collections.shuffle(teams);

        // Assign players in a round-robin fashion
        int index = 0;
        for (ServerPlayer player : playerContainer.getValues()) {
            String team = teams.get(index % teams.size());
            // Check if the player is online before performing actions
            Player onp = Bukkit.getPlayer(player.getUuid());
            if (onp != null && onp.isOnline()) {
                player.setTeamName(team);
                playerContainer.writeData(player.getUuid(), player);
                setPlayerScoreboardTeam(onp, team);
                setPlayerSpawnPoint(onp, team);
                if (onp.getGameMode() != GameMode.SURVIVAL && !isBystander(onp)) {
                    onp.setGameMode(GameMode.SURVIVAL);
                }
            } // else skip because player is null
            index++;
        }
    }

    // assign player to essentially a random team
    public static void assignPlayerRandomTeam(Player p){
        if (p == null || !p.isOnline()) {
            return;
        }
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        // Clear team leader status if the player was a team leader
        if (serverPlayer.isTeamLeader()) {
            serverPlayer.setTeamLeader(false);
        }

        // Ensure that scoreboard teams exist
        scoreboardTeams();

        // assign player to team with the lowest count
        String team = getTeamWithLowestCount();
        serverPlayer.setTeamName(team);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
        setPlayerScoreboardTeam(p, team);
        setPlayerSpawnPoint(p, team);
        if (p.getGameMode() != GameMode.SURVIVAL && !isBystander(p)) {
            p.setGameMode(GameMode.SURVIVAL);
        }

    }

    private static String getTeamWithLowestCount() {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        List<String> teams = getLiteralTeamNames();
        Collections.shuffle(teams); // randomize it a bit
        Map<String, Integer> teamCounts = new HashMap<>();
        for (String team : teams) {
            teamCounts.put(team, 0);
        }
        for (ServerPlayer player : playerContainer.getValues()) {
            String team = player.getTeamName();
            if (team != null) {
                teamCounts.put(team, teamCounts.get(team) + 1);
            }
        }
        int min = Collections.min(teamCounts.values());
        for (Map.Entry<String, Integer> entry : teamCounts.entrySet()) {
            if (entry.getValue() == min) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Create scoreboard teams
    public static void scoreboardTeams() {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String team : getLiteralTeamNames()) {
            String teamName = getTeamDisplayName(team);

            if (mainScoreboard.getTeam(teamName) == null) {
                mainScoreboard.registerNewTeam(teamName);
                updateScoreboardTeams(); // sets display name and color
            }
        }
    }

    // Update scoreboard team
    public static void updateScoreboardTeams() {
        for (String team : getLiteralTeamNames()) {
            String teamName = getTeamDisplayName(team);
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team scoreboardTeam = mainScoreboard.getTeam(teamName);

            if (scoreboardTeam != null) {
                scoreboardTeam.setColor(ChatColor.valueOf(getTeamColor(team)));
//                Bukkit.getConsoleSender().sendMessage("Team: " + teamName + " Color: " + getTeamColor(team)); // debug
                scoreboardTeam.setDisplayName(teamName);
            }
        }
    }

    // Set player's scoreboard team
    public static void setPlayerScoreboardTeam(Player player, String team) {
        String teamName = getTeamDisplayName(team);

        if (teamName == null) {
            Bukkit.getConsoleSender().sendMessage("§e[§bBattleKingdom§e] §cTeam name is null for team: " + team);
            return;
        }

        // if player is null or offline, skip them
        if (player == null || !player.isOnline()) {
            Bukkit.getConsoleSender().sendMessage("§e[§bBattleKingdom§e] §cSPST: §e" + player);
            return;
        }

        // Check if the player is online before accessing player.getName()
        if (player.isOnline()) {
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team scoreboardTeam = mainScoreboard.getTeam(teamName);

            if (scoreboardTeam != null) {
                scoreboardTeam.addEntry(player.getName());
            } else {
                Bukkit.getConsoleSender().sendMessage("§e[§bBattleKingdom§e] §cTeam: " + teamName + " does not exist.");
            }
        } // else skip because player is null
    }

    // Remove player's scoreboard team
    public static void removePlayerScoreboardTeam(Player p) {
        String teamName = getTeamDisplayName(p);  // get team name from player

        if (teamName != null && !teamName.isEmpty()) {
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team playerTeam = mainScoreboard.getTeam(teamName);

            if (playerTeam != null) {
                playerTeam.removeEntry(p.getName());
            } else {
                Bukkit.getConsoleSender().sendMessage("§e[§bBattleKingdom§e] §cTeam is null for player " + p.getName());
            }
        }
    }

    // Reset scoreboard teams
    public static void resetScoreboardTeams() {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (String team : getLiteralTeamNames()) {
            String teamName = getTeamDisplayName(team);
            Team scoreboardTeam = mainScoreboard.getTeam(teamName);

            if (scoreboardTeam != null) {
                scoreboardTeam.unregister();
            }
        }
        scoreboardTeams(); // Re-register scoreboard teams
    }

    public static String getTeamColorCode(String literalTeam) { // literal team name
        String key = "teams." + literalTeam + ".color";
        String color = plugin.getConfig().getString(key, "GRAY");
        color = color.toUpperCase();
        return switch (color) {
            case "GREEN" -> "§a";
            case "AQUA" -> "§b";
            case "RED" -> "§c";
            case "LIGHT_PURPLE" -> "§d";
            case "YELLOW" -> "§e";
            case "WHITE" -> "§f";
            case "BLACK" -> "§0";
            case "DARK_BLUE" -> "§1";
            case "DARK_GREEN" -> "§2";
            case "DARK_AQUA" -> "§3";
            case "DARK_RED" -> "§4";
            case "PURPLE" -> "§5";
            case "GOLD" -> "§6";
            case "GRAY" -> "§7";
            case "DARK_GRAY" -> "§8";
            case "BLUE" -> "§9";
            default -> "§r";
        };
    }
    public static String getTeamColor(String team) {
        String key = "teams." + team + ".color";
        String color = plugin.getConfig().getString(key, "GRAY");
        return color.toUpperCase();
    }
    public static void setTeamColor(String team, String color) {
        plugin.getConfig().set("teams." + team + ".color", color);
        updateScoreboardTeams();
        Bukkit.getScheduler().runTask(plugin, plugin::saveConfig);
    }
    public static void setTeamLeader(Player p, String team) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        // set old leader to false
        for (ServerPlayer player : playerContainer.getValues()) {
            if (player.getTeamName().equals(team) && player.isTeamLeader()) {
                player.setTeamLeader(false);
                UUID playerUUID = p.getUniqueId();
                playerContainer.writeData(playerUUID, player);
            }
        }
        // add player to team
        serverPlayer.setTeamName(team);
        // set new leader to true
        serverPlayer.setTeamLeader(true);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
        config.set("teams." + team + ".leader", p.getName());
        updateScoreboardTeams();
        Bukkit.getScheduler().runTask(plugin, plugin::saveConfig);

    }
    public static boolean isTeamLeader(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        return serverPlayer.isTeamLeader();
    }
    public static String getLeaderStatus(String team) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            if (team != null && team.equals(serverPlayer.getTeamName()) && serverPlayer.isTeamLeader()) {
                if (serverPlayer.isTeamRespawn()) {
                    return "§6§l" + serverPlayer.getPlayerName();
                } else {
                    return "§c§m" + serverPlayer.getPlayerName();
                }
            }
        }
        return null;
    }

    public static List<String> getLiteralTeamNames() {
        return new ArrayList<>(Objects.requireNonNull(config.getConfigurationSection("teams")).getKeys(false)); // get literal team names (TEAM1, TEAM2, etc.) not display names (Earth, Water, etc.)
    }
    public static String getLiteralTeamName(Player p) {
        if (p == null || !p.isOnline()) {
            return null;
        }
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        return serverPlayer.getTeamName();
    }
    public static String getTeamDisplayName(Player p) {
        String team = getLiteralTeamName(p);
        return plugin.getConfig().getString("teams." + team + ".displayname");
    }
    // convert literal team name to display name
    public static String getTeamDisplayName(String team) {
        return plugin.getConfig().getString("teams." + team + ".displayname");
    }
    public static void setTeamDisplayName(String teamName, String newName) {
        plugin.getConfig().set("teams." + teamName + ".displayname", newName);
        updateScoreboardTeams();
        Bukkit.getScheduler().runTask(plugin, plugin::saveConfig);
    }
    public static boolean getRespawnStatus(String team) {
        if(team != null) {
            PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
            for (ServerPlayer serverPlayer : playerContainer.getValues()) {
                String playerTeam = serverPlayer.getTeamName();
                if (team.equals(playerTeam) && serverPlayer.isTeamLeader()) {
                    return serverPlayer.isTeamRespawn();
                }
            }
        }
        return false;
    }

    public static void setRespawnStatus(String team, boolean status) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            if (serverPlayer.getTeamName().equals(team) && serverPlayer.isTeamLeader()) {
                serverPlayer.setTeamRespawn(status);
                playerContainer.writeData(serverPlayer.getUuid(), serverPlayer);
            }
        }
    }
    public static String getTeamMembers(String team) {
    
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
    
        List<String> teamMembers = new ArrayList<>();
    
        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            if (team.equals(serverPlayer.getTeamName())) {
                if (serverPlayer.isEliminated() && !serverPlayer.isTeamLeader()) {
                    teamMembers.add("§c§m" + serverPlayer.getPlayerName());
                } else if (!serverPlayer.isTeamLeader()) {
                    teamMembers.add("§a" + serverPlayer.getPlayerName());
                }
            }
        }
    
        if (teamMembers.isEmpty()) {
            return "No members in the team";
        }
    
        return String.join("§7, ", teamMembers);
    }

    // Hard reset ALL data
    public static void resetAllData() {
        // Delete scoreboard teams
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String team : getLiteralTeamNames()) {
            String teamName = getTeamDisplayName(team);
            Team scoreboardTeam = mainScoreboard.getTeam(teamName);
            if (scoreboardTeam != null) {
                scoreboardTeam.unregister();
            }
        }
        scoreboardTeams(); // Re-register scoreboard teams

        // Delete all player data
        String playersFolder = dataFolder + "/players";
        String configPath = dataFolder + "/config.yml";
        File players = new File(playersFolder);
        File config = new File(configPath);
        if (players.exists()) {
            File[] files = players.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete(); // delete all files in the 'players' folder
                }
            }
        }
        if (config.exists()) {
            config.delete(); // delete the config file
        }

        // Reset config
        plugin.getConfig().options().copyDefaults();
        plugin.saveDefaultConfig();


        // Reinstate online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
            ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
            serverPlayer.setPlayerName(p.getName());
            playerContainer.writeData(p.getUniqueId(), serverPlayer);
        }
        BattleKingdom.reload();
    }
//    resetPlayerData
    public static void resetPlayerData(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        removePlayerScoreboardTeam(p);
        serverPlayer.setTeamName(null);
        serverPlayer.setTeamLeader(false);
        serverPlayer.setTeamRespawn(false);
        serverPlayer.setEliminated(false);
        serverPlayer.setExemptFromKick(false);

        playerContainer.writeData(p.getUniqueId(), serverPlayer);
    }
    public static void resetTeamData(String team) {
        List<String> teams = getLiteralTeamNames();
        PlayerHandler playerHandler = PlayerHandler.getInstance();
        PlayerContainer playerContainer = playerHandler.getContainer();

        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            if (serverPlayer.getTeamName() != null && (team.equalsIgnoreCase("ALL") || serverPlayer.getTeamName().equals(team))) {
                resetPlayerTeamData(serverPlayer);
            }
        }
    }
    private static void resetPlayerTeamData(ServerPlayer serverPlayer) {
        // remove all players from team
        serverPlayer.setTeamName(null);
        serverPlayer.setTeamLeader(false);
        serverPlayer.setTeamRespawn(false);
        serverPlayer.setEliminated(false);
        serverPlayer.setTeamEliminated(false);
        resetScoreboardTeams();

        PlayerHandler.getInstance().getContainer().writeData(serverPlayer.getUuid(), serverPlayer);
    }

    public static boolean getTeamEliminated(String team) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            String playerTeamName = serverPlayer.getTeamName(); // get team name of player
            // serverPlayer.getTeamName() returns the literal team name (TEAM1, TEAM2, etc.)
            if (team != null && team.equals(playerTeamName) && serverPlayer.isTeamEliminated()) {
                return true;
            }
        }
        return false;
    }
    public static void setTeamEliminated(String team, boolean status) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        for (ServerPlayer serverPlayer : playerContainer.getValues()) {
            if (serverPlayer.getTeamName().equals(team)) {
                serverPlayer.setTeamEliminated(status);
                playerContainer.writeData(serverPlayer.getUuid(), serverPlayer);
            }
        }
    }
    public static void setSpectatorMode(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        TeamManager.setEliminated(p, true);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
        p.setGameMode(GameMode.SPECTATOR);
        p.spigot().respawn();
        Text.of("§7You're exempt from the dungeon, you are now a spectator.").send(p);
    }
    public static void setPlayerSpawnPoint(Player p, String team) {
        double x = plugin.getConfig().getDouble("teams." + team + ".x");
        double y = plugin.getConfig().getDouble("teams." + team + ".y");
        double z = plugin.getConfig().getDouble("teams." + team + ".z");
        String worldName = plugin.getConfig().getString("teams." + team + ".world");

        assert worldName != null;

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

        p.teleport(spawnLocation);
        p.setBedSpawnLocation(spawnLocation, true);
    }
    public static String getSpawnLocationForTeam(String team) {
        double x = plugin.getConfig().getDouble("teams." + team + ".x");
        double y = plugin.getConfig().getDouble("teams." + team + ".y");
        double z = plugin.getConfig().getDouble("teams." + team + ".z");

        x = Math.round(x * 100.0) / 100.0;
        y = Math.round(y * 100.0) / 100.0;
        z = Math.round(z * 100.0) / 100.0;

        return (x + ", " + y + ", " + z);
    }
    public static void setTeamSpawnPoint(String team, Location location) {
        plugin.getConfig().set("teams." + team + ".x", Math.round(location.getX() * 100.0) / 100.0);
        plugin.getConfig().set("teams." + team + ".y", Math.round(location.getY() * 100.0) / 100.0);
        plugin.getConfig().set("teams." + team + ".z", Math.round(location.getZ() * 100.0) / 100.0);
        plugin.getConfig().set("teams." + team + ".world", location.getWorld().getName());
        Bukkit.getScheduler().runTask(plugin, plugin::saveConfig);
    }
    public static void teleportToTeamSpawnPoint(Player p) {
        String team = getLiteralTeamName(p);
        double x = plugin.getConfig().getDouble("teams." + team + ".x");
        double y = plugin.getConfig().getDouble("teams." + team + ".y");
        double z = plugin.getConfig().getDouble("teams." + team + ".z");
        String worldName = plugin.getConfig().getString("teams." + team + ".world");

        assert worldName != null;

        Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z);
        Bukkit.getConsoleSender().sendMessage("Teleporting " + p.getName());
        p.teleport(spawnLocation);
    }

    public static void setExemptFromKick(Player p, boolean status) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        serverPlayer.setExemptFromKick(status);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
    }
    public static boolean isExemptFromKick(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        return serverPlayer.isExemptFromKick();
    }

    public static void setBystander(Player p, boolean status) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        serverPlayer.setBystander(status);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
    }
    public static boolean isBystander(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        return serverPlayer.isBystander();
    }

    public static void setEliminated(Player p, boolean status) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        serverPlayer.setEliminated(status);
        playerContainer.writeData(p.getUniqueId(), serverPlayer);
    }
    public static boolean isEliminated(Player p) {
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
        return serverPlayer.isEliminated();
    }
}
