package io.github.cats1337.battlekingdom.events;

import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.battlekingdom.BattleKingdom;
import io.github.cats1337.battlekingdom.playerdata.*;

import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    protected static final BattleKingdom plugin = BattleKingdom.getInstance();
    FileConfiguration config = BattleKingdom.getInstance().getConfig();



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());

        // check if player exists in container, if not add them
        if (serverPlayer.getPlayerName() == null) {
            serverPlayer.setPlayerName(p.getName());
            Bukkit.getScheduler().runTask(plugin, () -> playerContainer.writeData(p.getUniqueId(), serverPlayer));
            // if player is not on a team, assign them to a random team
        }
        if (TeamManager.getLiteralTeamName(p).equals("") || TeamManager.getLiteralTeamName(p) == null) {
            TeamManager.assignPlayerRandomTeam(p);
            TeamManager.setPlayerSpawnPoint(p, serverPlayer.getTeamName());
            Bukkit.getConsoleSender().sendMessage("Assigned " + p.getName() + " to " + serverPlayer.getTeamName());
        }


        serverPlayer.setPlayerName(p.getName()); // update player name

        // if in ban folder still, remove from ban folder
        String playerName = p.getName();
        PlayerHandler.checkBanLog(playerName, true);

        // if player is eliminated, set them to spectator mode
        if (serverPlayer.isEliminated() || !TeamManager.getRespawnStatus(TeamManager.getLiteralTeamName(p)) || TeamManager.isBystander(p) || serverPlayer.isExemptFromKick() || serverPlayer.isTeamLeader() || TeamManager.getLiteralTeamName(p) != null) {
            p.setGameMode(GameMode.SPECTATOR);
        } else {
            if (serverPlayer.getTeamName() != null) {
                // wait for player to fully join server before teleporting
                if (p.isOnline()) {
                    Bukkit.getScheduler().runTaskLater(BattleKingdom.getInstance(), () -> {
                        if (p.getHealth() <= 0.0D && p.isOnline()) { // if player is dead and online
                            p.spigot().respawn(); // respawn the player
                        }
                        p.setGameMode(GameMode.SURVIVAL); // set player to survival mode
                        TeamManager.teleportToTeamSpawnPoint(p);
                    }, 60L);
                }
            }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        UUID playerUUID = p.getUniqueId();
        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
        ServerPlayer serverPlayer = playerContainer.loadData(playerUUID);

        String teamName = serverPlayer.getTeamName();
        String teanDisName = TeamManager.getTeamDisplayName(p);
        if (serverPlayer.isTeamLeader()) {
            TeamManager.setRespawnStatus(teamName, false);
//            get leaders team
            String leaderTeam = serverPlayer.getTeamName(); // get the team name of the leader
            Text.of(TeamManager.getTeamColorCode(teamName) + "§l§n" + teanDisName + "'s§e §l👑 §6§lKing §e§l👑 §chas been §4vanquished!").send(Bukkit.getOnlinePlayers());
            for (ServerPlayer player : playerContainer.getValues()) {
                // check if the player is on the same team as the leader
                if (player.getTeamName().equals(leaderTeam)) {
                    Player teamPlayer = Bukkit.getPlayer(player.getUuid());
                    if (teamPlayer != null) {
                        Text.of("&c&oYou will no longer respawn!").send(teamPlayer);
                        teamPlayer.sendTitlePart(TitlePart.TITLE, Component.text("§cYour §e§l👑 §chas been §4vanquished!"));
                        teamPlayer.sendTitlePart(TitlePart.SUBTITLE, Component.text("§c§oYou will no longer respawn!"));
                        teamPlayer.sendTitlePart(TitlePart.TIMES, Title.Times.times(java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(1)));
                    }
                }
                // if not on the same team as the leader
                if (!player.getTeamName().equals(leaderTeam)) {
                    Player onp = Bukkit.getPlayer(player.getUuid());
                    if (onp != null) {
                        onp.sendTitlePart(TitlePart.TITLE, Component.text(TeamManager.getTeamColorCode(teamName) + "§l§n" + TeamManager.getTeamDisplayName(p) + "§c's §e§l👑 §chas been §4vanquished!"));
                        onp.sendTitlePart(TitlePart.SUBTITLE, Component.text("§cThey will no longer respawn!"));
                        onp.sendTitlePart(TitlePart.TIMES, Title.Times.times(java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(1)));
                        onp.playSound(onp.getLocation(), "entity.lightning_bolt.thunder", 1.0F, 1.0F);
                    }
                }
            }
        }

        if (!TeamManager.getRespawnStatus(teamName)) {
            p.setGameMode(GameMode.SPECTATOR);
            if (serverPlayer.isExemptFromKick() || serverPlayer.isTeamLeader()) {
                TeamManager.setSpectatorMode(p);
                TeamManager.setEliminated(p, true);
            }
            if (!serverPlayer.isExemptFromKick() && !serverPlayer.isTeamLeader() && config.getBoolean("DUNGEON")) {
                PlayerHandler.logBannedPlayer(p, teamName);
                PlayerHandler.tempBanPlayer(p, config.getInt("DUNGEON_TIME"), config.getString("DUNGEON_MESSAGE"));
                PlayerHandler.setIsEliminated(p.getName(), true);
            }
        }

        // check if anyone on the team is still alive
        for (ServerPlayer player : playerContainer.getValues()) {
            if (player.getTeamName().equals(teamName) && !player.isEliminated()) {
                TeamManager.setTeamEliminated(teamName, false);
                return;
            } else if (player.getTeamName().equals(teamName)){ // no one on the team is alive on the server
                TeamManager.setTeamEliminated(teamName, true);
                // kill all offline players on the team
                for (ServerPlayer offlinePlayer : playerContainer.getValues()) {
                    if (offlinePlayer.getTeamName().equals(teamName)) {
                        Player offline = Bukkit.getPlayer(offlinePlayer.getUuid());
                        if (offline == null) {
                            PlayerHandler.setIsEliminated(offlinePlayer.getPlayerName(), true);
                            Player ofp = Bukkit.getOfflinePlayer(offlinePlayer.getUuid()).getPlayer();
                            if (ofp != null) {
                                PlayerHandler.tempBanPlayer(ofp, config.getInt("DUNGEON_TIME"), config.getString("DUNGEON_MESSAGE"));
                            }
                        }
                    }
                }
            }
        }
        if (TeamManager.getTeamEliminated(teamName)) {
            Text.of(TeamManager.getTeamColorCode(teamName) + "§l§n" + TeamManager.getTeamDisplayName(p) + "§c has been &celiminated!").send(Bukkit.getOnlinePlayers());
            // send title to all players online
            for (Player onp : Bukkit.getOnlinePlayers()) {
                onp.sendTitlePart(TitlePart.TITLE, Component.text(TeamManager.getTeamColorCode(teamName) + "§l§n" + TeamManager.getTeamDisplayName(p)));
                onp.sendTitlePart(TitlePart.SUBTITLE, Component.text("§chas been eliminated!"));
                onp.sendTitlePart(TitlePart.TIMES, Title.Times.times(java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(2), java.time.Duration.ofSeconds(1)));
                onp.playSound(onp.getLocation(), "entity.lightning_bolt.thunder", 1.0F, 2.0F);
            }
        }
    }
}
