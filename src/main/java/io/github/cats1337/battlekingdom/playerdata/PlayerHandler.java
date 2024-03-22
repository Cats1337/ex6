package io.github.cats1337.battlekingdom.playerdata;


import io.github.cats1337.battlekingdom.BattleKingdom;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class PlayerHandler implements Listener {
    private static PlayerHandler instance;
    public PlayerHandler() {
        instance = this;
    }
    static final BattleKingdom plugin = BattleKingdom.getInstance();
    protected final static String dataFolder = plugin.getDataFolder().getAbsolutePath(); // plugins/BattleKingdom

    public static PlayerHandler getInstance() {
        return Objects.requireNonNullElseGet(instance, PlayerHandler::new);
    }

    public @NotNull Collection<ServerPlayer> getGamePlayers() {return getContainer().getValues();} // Remove?

    public PlayerContainer getContainer() {
        if (BattleKingdom.getInstance().getContainerManager().getByType(PlayerContainer.class).isEmpty()){
            return null;
        }
        return BattleKingdom.getInstance().getContainerManager().getByType(PlayerContainer.class).get();
    }

    public static void tempBanPlayer(Player p, int dungeonTime, String reason) {
        long durationInMillis = (long) dungeonTime * 60 * 60 * 1000;
        Date banTime = new Date(System.currentTimeMillis() + durationInMillis);

        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        PlayerProfile profile = (p.getPlayerProfile());
        BanEntry<PlayerProfile> banEntry = banList.getBanEntry(profile);

        if (banEntry == null) {
            banList.addBan(profile, ("§c" + reason), banTime, null);
            p.kickPlayer("§c" + reason);
        } else {
            banEntry.setExpiration(banTime);
        }

        // log banned players name & team for unbanning later
    }
    
    public static boolean untempbanPlayer(String p){
        OfflinePlayer ofp = Bukkit.getOfflinePlayer(p);
        if (ofp.isBanned()) {
            BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
            PlayerProfile profile = Bukkit.createProfile(ofp.getUniqueId(), p);
            BanEntry<PlayerProfile> banEntry = banList.getBanEntry(profile);
            if (banEntry != null) {
                checkBanLog(p, true);
                banList.pardon(profile);
                return true;
            }
        }

        return false;
    }

    // checkBanLog
    public static boolean checkBanLog(String p, boolean remove) {
        // get playername from string p
        // if player is online, get their username
        if (Bukkit.getPlayer(p) != null) {
            p = Bukkit.getPlayer(p).getName(); // get player's username
        }
        OfflinePlayer ofp = Bukkit.getOfflinePlayer(p); // get player's username, changes nothing if player is online

        // get player's team from playerdata
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(ofp.getUniqueId());
        String teamName = serverPlayer.getTeamName();

        // get team's ban file
        File teamBanFile = new File(plugin.getDataFolder().getAbsolutePath() + "/bans/" + teamName + ".json");
        JSONArray banArray = new JSONArray();

        if (teamBanFile.exists()) {
            // If the file exists, read the existing data into a JSONArray
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader(teamBanFile)) {
                Object obj = jsonParser.parse(reader);
                banArray = (JSONArray) obj;
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        // loop through the JSONArray and check if the player is banned
        for (Object o : banArray) {
            JSONObject playerData = (JSONObject) o;
            String uuid = (String) playerData.get("uuid");
            String playerName = (String) playerData.get("playerName");
            if (playerName.equals(p)) {
                if (remove) {
                    banArray.remove(playerData);
                    try (FileWriter file = new FileWriter(teamBanFile)) {
                        file.write(banArray.toJSONString());
                        file.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }


    public static void untempbanTeam(String bannedTeam){
        // get banned player name, uuid & team from "bans/teamname.json" for unbanning
        File teamBanFile = new File(plugin.getDataFolder().getAbsolutePath() + "/bans/" + bannedTeam + ".json");
        JSONArray banArray = new JSONArray();

        if (teamBanFile.exists()) {
            // If the file exists, read the existing data into a JSONArray
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader(teamBanFile)) {
                Object obj = jsonParser.parse(reader);
                banArray = (JSONArray) obj;
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        // loop through the JSONArray and unban each player
        for (Object o : banArray) {
            JSONObject playerData = (JSONObject) o;
            String uuid = (String) playerData.get("uuid");
            String playerName = (String) playerData.get("playerName");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (offlinePlayer.isBanned()) {
                BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                PlayerProfile profile = Bukkit.createProfile(offlinePlayer.getUniqueId(), playerName);
                BanEntry<PlayerProfile> banEntry = banList.getBanEntry(profile);
                if (banEntry != null) {
                    banList.pardon(profile);
                    return;
                }
            }
        }
    }

    // log banned player name, uuid & team in a json file called "bans/teamname.json" for unbanning later
    public static void logBannedPlayer(Player p, String bannedTeam) {
    // Create a folder if it doesn't exist
        File bansFolder = new File(plugin.getDataFolder(), "bans");
        if (!bansFolder.exists()) {
            if (!bansFolder.mkdir()) {
                Bukkit.getConsoleSender().sendMessage("§e[§bBattleKingdom§e] §cError creating bans folder");
                return;
            }
        }

        // check if bans/teamname.json exists
        File teamBanFile = new File(bansFolder + "/" + bannedTeam + ".json");
        JSONArray banList = new JSONArray();

        // if file doesn't exist, create it
        if (!teamBanFile.exists()) {
            try {
                teamBanFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // get player name, uuid, team
        UUID uuid = p.getUniqueId();
        String playerName = p.getName();

        // Create a JSONObject to hold the player's data
        JSONObject playerData = new JSONObject();
        playerData.put("uuid", uuid.toString());
        playerData.put("playerName", playerName);
        playerData.put("bannedTeam", bannedTeam);


        if (teamBanFile.exists()) {
            // If the file exists, read the existing data into a JSONArray
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader(teamBanFile)) {
                Object obj = jsonParser.parse(reader);
                banList = (JSONArray) obj;
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        // Add the new JSONObject to the JSONArray
        banList.add(playerData);

        // Write the JSONArray back to the file
        try (FileWriter file = new FileWriter(teamBanFile)) {
            file.write(banList.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean setIsEliminated(String p, boolean status) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p);
        // set player to not eliminated
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(offlinePlayer.getUniqueId());

        serverPlayer.setEliminated(status);
        playerContainer.writeData(offlinePlayer.getUniqueId(), serverPlayer);
        return true;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ServerPlayer serverPlayer = getContainer().loadData(uuid);
        if (serverPlayer.getPlayerName() == null) {
            serverPlayer.setPlayerName(p.getName());
            getContainer().writeData(uuid, serverPlayer);
        } else {
            serverPlayer.setPlayerName(p.getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ServerPlayer serverPlayer = getContainer().loadData(uuid);
        getContainer().writeData(uuid, serverPlayer);
    }
}
