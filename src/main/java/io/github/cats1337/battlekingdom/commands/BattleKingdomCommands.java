package io.github.cats1337.battlekingdom.commands;

import com.marcusslover.plus.lib.command.TabCompleteContext;
import com.marcusslover.plus.lib.text.Text;
import io.github.cats1337.battlekingdom.BattleKingdom;
import io.github.cats1337.battlekingdom.playerdata.PlayerContainer;
import io.github.cats1337.battlekingdom.playerdata.PlayerHandler;
import io.github.cats1337.battlekingdom.playerdata.ServerPlayer;
import io.github.cats1337.battlekingdom.playerdata.TeamManager;
import com.marcusslover.plus.lib.command.Command;
import com.marcusslover.plus.lib.command.CommandContext;
import com.marcusslover.plus.lib.command.ICommand;
import io.github.cats1337.battlekingdom.utils.ITabCompleterHelper;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// import static io.github.cats1337.battlekingdom.playerdata.TeamManager.*;

@Command(name = "kingdom")
public class BattleKingdomCommands implements ICommand {

    // Command: /kingdom

    // HELP - permission: kingdom.help
    // /kingdom help - lists all commands that the player has permission to use

    // SET - permission: kingdom.set
    // /kingdom set <team|leader|spawn>
        // /kingdom set <team> <player> - sets a player's team
        // /kingdom set leader <team> <player> - sets team leader
        // /kingdom set spawn <team> - sets team spawn point
        // /kingdom set name <team> <name> - sets a team's name
        // /kingdom set color <team> <color> - sets a team's color
        // /kingdom set exempt <player> - sets a player as exempt from kick

    // RANDOMTEAMS - permission: kingdom.randomteams
    // /kingdom randomteams <all|player> - randomly assigns players to teams
        // /kingdom randomteams all - randomly assigns all players to teams
        // /kingdom randomteams player <player> - randomly assigns a specific player to a team

    // INFO - permission: kingdom.info
    // /kingdom info <team|player> - lists team info or player info
        // /kingdom info <team> - lists team info (team name, team color, team alive, team spawn point, team leader, team members)
        // /kingdom info <player> - lists player info (player name, is leader or not, player team, player alive)

    // LIST - permission: kingdom.list
    // /kingdom list <team> - lists team info - team name, team leader, team status (alive or eliminated), list team members

    // RESPAWN - permission: kingdom.respawn
    // /kingdom respawn <player|team> - unbans/respawns a player or revives a team
        // /kingdom respawn player <player> - unbans/respawns a player
        // /kingdom respawn team <team> - revives a team

    // KICK - permission: kingdom.kick
    // /kingdom kick <player> - kicks a player from their team

    // RESET - permission: kingdom.reset
    // /kingdom reset <all|player|team> - resets all player data, specific player's data, or a specific team's data (including team leader, team spawn, and team members)
        // /kingdom reset all - resets all player data
        // /kingdom reset player <player> - resets a specific player's data
        // /kingdom reset team <team> - resets a specific team's data

    private final List<String> subCommands = List.of("help", "set", "randomteams", "info", "list", "respawn", "kick", "reset");
    private final List<String> publicSubCommands = List.of("help", "info", "list");
    static final BattleKingdom plugin = BattleKingdom.getInstance();
    @Override
    public boolean execute(@NotNull CommandContext cmd){
        CommandSender sender = cmd.sender();
        String[] args = cmd.args();
        if (args.length == 0) {
            Text.of("§cYou must specify a subcommand.").send(sender);
            return true;
        } else {
            String arg = args[0];
            if (arg.equalsIgnoreCase("help")) {
                    Text.of("§b[« §4§l💠 §b»] §6§l§nBattle§e§l 👑 §6§l§nKingdom&9§l Help&r§b [« §4§l💠 §b»]").send(sender);
                    if (sender.hasPermission("kingdom.set")) {
                        Text.of("§f/kingdom set §e<team> <player> §7-§6 Sets player's team\n" +
                                "§f/kingdom set leader §e<team> <player> §7-§6 Sets team's leader\n" +
                                "§f/kingdom set spawn §e<team> §7-§6 Sets team's spawn point\n" +
                                "§f/kingdom set name §e<team> <name> §7-§6 Set's a team's name\n" +
                                "§f/kingdom set color §e<team> <color> §7-§6 Set's a team's color\n" +
                                "§f/kingdom set exempt §e<player> kick <true|false> §7-§6 Set's a player as exempt from kick\n" +
                                "§f/kingdom set exempt §e<player> bystander <true|false> §7-§6 Set's a player as a bystander"
                        ).send(sender);
                    }
                    if (sender.hasPermission("kingdom.randomteams")) {
                        Text.of("§f/kingdom randomteams all §7-§6 Randomly assigns all players to teams\n" +
                                "§f/kingdom randomteams player §e<player> §7-§6 Randomly assigns a specific player to a team"
                        ).send(sender);
                    }
                    Text.of("§f/kingdom <info|list> §e<player|team> §7-§6 Lists player/team info").send(sender);
                    if (sender.hasPermission("kingdom.respawn")) {
                        Text.of("§f/kingdom respawn §e<player> §7-§6 Revives a player\n" +
                                "§f/kingdom respawn §e<team> §7-§6 Revives a team"
                        ).send(sender);
                    }
                    if (sender.hasPermission("kingdom.kick")) {
                        Text.of("§f/kingdom kick §e<player> §7-§6 Kicks a player from their team"
                        ).send(sender);
                    }
                    if (sender.hasPermission("kingdom.reset")) {
                        Text.of("§f/kingdom reset §e<all> §7- §4§lResets all player data\n" +
                                "§f/kingdom reset §e<player> §7-§6 Resets a specific player's data\n" +
                                "§f/kingdom reset §e<team> §7-§6 Resets a specific team's data"
                        ).send(sender);
                    }
                    Text.of("§f/kingdom help §7-§6 To display this message").send(sender);

                return true;
            }
            switch (arg) {
                case "set":
                    if (args.length == 1) {
                        Text.of("§cInvalid Command Usage.").send(sender);
                        Text.of("§f/kingdom set §e<team|leader|spawn>\n" +
                                "§7run /kingdom help for more info"
                        ).send(sender);
                        return true; // if no subcommand is specified, return true, breaking out of the switch statement
                    } else { // if a subcommand is specified, continue
                        String subArg = args[1];
                        switch (subArg) {
                            case "team":
                                if (args.length < 3) {
                                    Text.of("§cUsage: /kingdom set team <team> <player>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.team")) {
                                    String team = args[2];
                                    if (args.length == 3) {
                                        Text.of("§cYou must specify a player.").send(sender);
                                        return true;
                                    } else {
                                        String playerName = args[3];
                                        Player p = Bukkit.getPlayer(playerName);
                                        if (p == null) {
                                            Text.of("§cPlayer not found.").send(sender);
                                            return true;
                                        }
                                        UUID playerUUID = p.getUniqueId();
                                        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                        ServerPlayer serverPlayer = playerContainer.loadData(playerUUID);
                                        serverPlayer.setTeamName(team);
                                        TeamManager.setPlayerScoreboardTeam(p, team);
                                        TeamManager.setPlayerSpawnPoint(p, team);
                                        serverPlayer.setTeamLeader(false);
                                        serverPlayer.setEliminated(false); // revive player
                                        playerContainer.writeData(playerUUID, serverPlayer);
                                        Text.of("§aSuccessfully set " + playerName + "'s team to " + team + ".").send(sender);
                                    }
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "leader":
                                if (args.length < 3) {
                                    Text.of("§cUsage: /kingdom set leader <team> <player>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.leader")) {
                                    String team = args[2].toUpperCase();
                                    if (args.length == 3) {
                                        Text.of("§cYou must specify a player.").send(sender);
                                        return true;
                                    } else {
                                        String playerName = args[3];
                                        Player p = Bukkit.getPlayer(playerName);
                                        if (p == null) {
                                            Text.of("§cPlayer not found.").send(sender);
                                            return true;
                                        }
                                        TeamManager.setTeamLeader(p, team);
                                        // set team status to alive
                                        TeamManager.setRespawnStatus(team, true);
                                        TeamManager.setEliminated(p, false);
                                        TeamManager.setTeamEliminated(team, false);
                                        Text.of("§aSuccessfully set " + p.getName() + " as the leader of " + team + ".").send(sender);
                                    }
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "spawn":
                                if (args.length < 3) {
                                    Text.of("§cUsage: /kingdom set spawn <team>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.spawn")){
                                    String team = args[2];
                                    Player p = (Player) sender;
                                    TeamManager.setTeamSpawnPoint(team, p.getLocation());
                                    Text.of("§aSuccessfully set " + team + "'s spawn point.").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "name":
                                if (args.length < 3) {
                                    Text.of("§cUsage: /kingdom set name <team> <name>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.name")){
                                    String team = args[2];
                                    String name = args[3];
                                    TeamManager.setTeamDisplayName(team, name);
                                    Text.of("§aSuccessfully set " + team + "'s name to " + name + ".").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                                }
                            case "color":
                                if (args.length < 3) {
                                    Text.of("§cUsage: /kingdom set color <team> <color>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.color")){
                                    String team = args[2];
                                    String color = args[3];
                                    TeamManager.setTeamColor(team, color);
                                    Text.of("§aSuccessfully set " + team + "'s color to " + color + ".").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                                }
                            case "exempt":
                                if (args.length < 5) {
                                    Text.of("§cUsage: /kingdom set exempt <player> <kick|bystander> <true|false>").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.set.exempt")){
                                    String playerName = args[2];
                                    String type = args[3];
                                    String bool = args[4];
                                    Player p = Bukkit.getPlayer(playerName);
                                    if (p == null) {
                                        Text.of("§cPlayer not found.").send(sender);
                                        return true;
                                    }
                                    if (type.equals("kick") && sender.hasPermission("kingdom.set.exempt.kick")) {
                                        if (bool.equalsIgnoreCase("false")) {
                                            TeamManager.setExemptFromKick(p, false);
                                            Text.of("§aSuccessfully set " + playerName + " no longer exempt from kick.").send(sender);
                                            return true;
                                        } else if (bool.equalsIgnoreCase("true")) {
                                            TeamManager.setExemptFromKick(p, true);
                                            Text.of("§aSuccessfully set " + playerName + " as exempt from kick.").send(sender);
                                            return true;
                                        } else {
                                            Text.of("§cInvalid argument. Use true or false.").send(sender);
                                            return true;
                                        }
                                    }
                                    if (type.equals("bystander") && sender.hasPermission("kingdom.set.exempt.bystander")) {
                                        if (bool.equalsIgnoreCase("false")) {
                                            TeamManager.setBystander(p, false);
                                            Text.of("§aSuccessfully set " + playerName + " no longer a bystander.").send(sender);
                                            return true;
                                        } else if (bool.equalsIgnoreCase("true")) {
                                            TeamManager.setBystander(p, true);
                                            Text.of("§aSuccessfully set " + playerName + " as a bystander.").send(sender);
                                            return true;
                                        } else {
                                            Text.of("§cInvalid argument. Use true or false.").send(sender);
                                            return true;
                                        }
                                    }
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                                }
                            }
                            return true;
                    }
                case "randomteams":
                    if (args.length < 2) {
                        Text.of("§cInvalid Command Usage.").send(sender);
                        Text.of("\n§f/kingdom randomteams §e<all|player>\n" +
                                "§7run /kingdom help for more info"
                        ).send(sender);
                        return true;
                    } else {
                        String subArg = args[1];
                        switch (subArg) {
                            case "all":
                                if (sender.hasPermission("kingdom.randomteams.all")) {
                                    TeamManager.resetTeamData("ALL"); // reset all team data
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p != null && !TeamManager.isBystander(p)) { // Add null check here
                                            TeamManager.assignAllRandomTeam(p);
                                        }
                                    }
                                    Text.of("§aSuccessfully assigned random teams to all players.").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "player":
                                if (args.length == 2 && sender.hasPermission("kingdom.randomteams.player")) {
                                    Text.of("§cYou must specify a player.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.randomteams.player")){
                                    String playerName = args[2];
                                    Bukkit.getOnlinePlayers().forEach(p -> {
                                        if (p.getName().equals(playerName)) {
                                            TeamManager.assignPlayerRandomTeam(p);
                                            Text.of("§aSuccessfully assigned a random team to " + playerName + ".").send(sender);
                                        }
                                    });
                                    return true;
                                } else {
                                Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                            }
                        }
                        return true;
                    }
                case "info", "list":
                    if (args.length < 2) {
                        Text.of("§cInvalid Command Usage.").send(sender);
                        Text.of("\n§f/kingdom info §e<team|player>\n" +
                                "§7run /kingdom help for more info"
                        ).send(sender);
                        return true;
                    } else {
                        String subArg = args[1];
                        switch (subArg) {
                            case "team":
                                if (args.length < 3 && (sender.hasPermission("kingdom.info.team") || sender.hasPermission("kingdom.list.team"))) {
                                    Text.of("§cYou must specify a team.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.info.team") || sender.hasPermission("kingdom.list.team")) {
                                    String team = args[2];
                                    StringBuilder teamInfo = new StringBuilder();
                                    teamInfo.append("§b[« §4§l💠 §b»] " + TeamManager.getTeamColorCode(team) + "§l§n" + TeamManager.getTeamDisplayName(team) + "§b [« §4§l💠 §b»]\n");
                                    if (TeamManager.getTeamEliminated(team)){
                                        teamInfo.append("§7Eliminated: §c" + TeamManager.getTeamEliminated(team) + "\n");
                                    } else {
                                        teamInfo.append("§7Eliminated: §a" + TeamManager.getTeamEliminated(team) + "\n");
                                    }
                                    if (TeamManager.getRespawnStatus(team)){
                                            teamInfo.append("§7Respawn Status: §a" + TeamManager.getRespawnStatus(team) + "\n");
                                    } else {
                                        teamInfo.append("§7Respawn Status: §c" + TeamManager.getRespawnStatus(team) + "\n");
                                    }
                                    if (sender.hasPermission("kingdom.admin")) {
                                        teamInfo.append("§7Spawn Point: " + TeamManager.getSpawnLocationForTeam(team) + "\n")
                                                .append("§7Color: " + TeamManager.getTeamColorCode(team) + TeamManager.getTeamColor(team) + "\n");
                                    }
                                    teamInfo.append("§e§l👑 §7Leader: §6" + TeamManager.getLeaderStatus(team) + "\n")
                                            .append("§7Members: " + TeamManager.getTeamMembers(team));

                                    Text.of(teamInfo.toString()).send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "player":
                                if (args.length < 3 && (sender.hasPermission("kingdom.info.player") || sender.hasPermission("kingdom.list.player"))) {
                                    Text.of("§cYou must specify a player.").send(sender);
                                    return true;
                                } else if ((sender.hasPermission("kingdom.info.player") || sender.hasPermission("kingdom.list.player"))) {
                                    String playerName = args[2];
                                    Player p = Bukkit.getPlayer(playerName);
                                    if (p == null) {
                                        Text.of("§cPlayer offline/not found.").send(sender);
                                        return true;
                                    }
                                    StringBuilder playerInfo = new StringBuilder();

                                    if (TeamManager.isTeamLeader(p)) {
                                        playerInfo.append("§e§l👑 §6§lleader§7 ");
                                    }
                                    playerInfo.append(TeamManager.getTeamColorCode(TeamManager.getLiteralTeamName(p)) + p.getName() + "§7 is on " + TeamManager.getTeamColorCode(TeamManager.getLiteralTeamName(p)) + "§l§n" + TeamManager.getTeamDisplayName(p));

                                    if (sender.hasPermission("kingdom.admin")) {
                                        if (TeamManager.isExemptFromKick(p)) {
                                            playerInfo.append("\n§7Exempt:§a " + TeamManager.isExemptFromKick(p));
                                        } else {
                                            playerInfo.append("\n§7Exempt:§c " + TeamManager.isExemptFromKick(p));
                                        }
                                        // check if player is banned
                                        if (PlayerHandler.checkBanLog(playerName, false)) {
                                            playerInfo.append("\n§7Banned: §c" + PlayerHandler.checkBanLog(playerName, false));
                                        } else {
                                            playerInfo.append("\n§7Banned: §a" + PlayerHandler.checkBanLog(playerName, false));
                                        }
                                    }
                                    if(TeamManager.getRespawnStatus(TeamManager.getLiteralTeamName(p))) {
                                        playerInfo.append("\n§7Team Respawn: §a" + TeamManager.getRespawnStatus(TeamManager.getLiteralTeamName(p)));
                                    } else {
                                        playerInfo.append("\n§7Team Respawn: §c" + TeamManager.getRespawnStatus(TeamManager.getLiteralTeamName(p)));
                                    }
                                    if(TeamManager.isEliminated(p)) {
                                        playerInfo.append("\n§7Eliminated: §c" + TeamManager.isEliminated(p));
                                    } else {
                                        playerInfo.append("\n§7Eliminated: §a" + TeamManager.isEliminated(p));
                                    }

                                    Text.of(playerInfo.toString()).send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                        }
                        return true;
                    }
                case "respawn":
                    if (args.length < 2){
                        Text.of("§cInvalid Command Usage.").send(sender);
                        Text.of("\n§f/kingdom respawn §e<player|team>\n" +
                                "§7run /kingdom help for more info"
                        ).send(sender);
                        return true;
                    } else {
                        String subArg = args[1];
                        switch (subArg) {
                            case "player":
                                if (args.length < 3 && sender.hasPermission("kingdom.respawn.player")) {
                                    Text.of("§cYou must specify a player.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.respawn.player")) {
                                    String playerName = args[2];
                                    Bukkit.getOnlinePlayers().forEach(p -> {
                                        if (p.getName().equals(playerName)) {
                                            PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                            ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
                                            if (!TeamManager.getRespawnStatus(serverPlayer.getTeamName())) {
                                                TeamManager.setRespawnStatus(serverPlayer.getTeamName(), true);
                                                TeamManager.setEliminated(p, false);
                                                TeamManager.teleportToTeamSpawnPoint(p);
                                                p.spigot().respawn();
                                                p.setGameMode(GameMode.SURVIVAL);
                                            }
                                            Text.of("§aSuccessfully revived " + p.getName() + ".").send(sender);
                                            if (serverPlayer.isExemptFromKick()) {
                                                TeamManager.teleportToTeamSpawnPoint(p);
                                                TeamManager.setEliminated(p, false);
                                                p.spigot().respawn();
                                                p.setGameMode(GameMode.SURVIVAL);
                                            }
                                            if (serverPlayer.isTeamLeader()) {
                                                TeamManager.teleportToTeamSpawnPoint(p);
                                                p.spigot().respawn();
                                                p.setGameMode(GameMode.SURVIVAL);
                                                TeamManager.setRespawnStatus(serverPlayer.getTeamName(), true);
                                                TeamManager.setEliminated(p, false);
                                                TeamManager.setTeamEliminated(serverPlayer.getTeamName(), false);
                                                Text.of("§aLeader Revived, " + TeamManager.getTeamColor(TeamManager.getLiteralTeamName(p)) + TeamManager.getTeamDisplayName(p) + " revived.").send(sender);
                                            }
                                        }
                                    });
                                     if (PlayerHandler.untempbanPlayer(playerName) && PlayerHandler.setIsEliminated(playerName, false)) {
                                         Text.of("§aSuccessfully revived " + playerName + ".").send(sender);
                                         return true;
                                     } else if (!PlayerHandler.untempbanPlayer(playerName)) {
                                            Text.of("§cUnable to revive " + playerName + ", not found.").send(sender);
                                     }
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "team":
                                if (args.length < 3 && sender.hasPermission("kingdom.respawn.team")) {
                                    Text.of("§cYou must specify a team.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.respawn.team")) {
                                    String team = args[2].toUpperCase();
                                    // online players
                                    Bukkit.getOnlinePlayers().forEach(p -> {
                                        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
                                        if (serverPlayer.getTeamName().equals(team)) {
                                            TeamManager.setEliminated(p, false);
                                            TeamManager.teleportToTeamSpawnPoint(p);
                                            PlayerHandler.setIsEliminated(p.getName(), false);
                                            Player onp = Bukkit.getPlayer(p.getUniqueId());
                                            if (onp != null) {
                                                PlayerHandler.untempbanPlayer(onp.getName());
//                                                Text.of("§aRevived " + onp + ".").send(sender); // Debug
                                            }
                                            p.spigot().respawn();
                                            p.setGameMode(GameMode.SURVIVAL);
                                        }
                                    });

                                    // offline players
                                    PlayerHandler.untempbanTeam(team);
                                    TeamManager.setRespawnStatus(team, true);
                                    TeamManager.setTeamEliminated(team, false);
                                    PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                    for (ServerPlayer offlinePlayer : playerContainer.getValues()) {
                                        if (offlinePlayer.getTeamName().equals(team)) {
                                            Player offline = Bukkit.getPlayer(offlinePlayer.getUuid());
                                            if (offline == null) {
                                                PlayerHandler.setIsEliminated(offlinePlayer.getPlayerName(), false);
                                                Player ofp = Bukkit.getOfflinePlayer(offlinePlayer.getUuid()).getPlayer();
                                                if (ofp != null) {
                                                    TeamManager.setEliminated(ofp, false);
                                                    TeamManager.teleportToTeamSpawnPoint(ofp);
                                                }
                                            }
                                        }
                                    }
                                    Text.of("§aRevived " + team + ".").send(sender);
                                    return true;
                                } else {
                                Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                                }
                        }
                        return true;
                    }

                case "kick":
                    if (args.length == 1) {
                        Text.of("§cInvalid subcommand.\n" +
                                "§7kick <§bplayer§7>").send(sender);
                        return true;
                    } else {
                        String subArg = args[1];
                        if (subArg.equals("player")) {
                            if (args.length == 2 && sender.hasPermission("kingdom.kick.player")) {
                                Text.of("§cYou must specify a player.").send(sender);
                                return true;
                            } else if (sender.hasPermission("kingdom.kick.player")) {
                                String playerName = args[2];
                                Bukkit.getOnlinePlayers().forEach(p -> {
                                    if (p.getName().equals(playerName)) {
                                        PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                        ServerPlayer serverPlayer = playerContainer.loadData(p.getUniqueId());
                                        serverPlayer.setTeamName("");
                                        TeamManager.removePlayerScoreboardTeam(p);
                                        if (serverPlayer.isTeamLeader()) {
                                            Text.of("§cYou cannot kick the team leader.").send(sender);
                                        } else {
                                            Text.of("§aSuccessfully kicked " + p.getName() + ".").send(sender);
                                        }
                                    }
                                });
                                return true;
                            } else {
                                Text.of("§cYou do not have permission to use this command.").send(sender);
                                return true;
                            }
                        }
                        return true;
                    }
                case "reset":
                    if (args.length < 2) {
                        Text.of("§cInvalid Command Usage.").send(sender);
                        Text.of("\n§f/kingdom reset §e<all|player|team>\n" +
                                "§7run /kingdom help for more info"
                        ).send(sender);
                        return true;
                    } else {
                        String subArg = args[1];
                        switch (subArg) {
                            // require 'all' to be used with caution, by having a confirmation prompt
                            case "all":
                                if (args.length == 2 && sender.hasPermission("kingdom.reset.all")) {
                                    Text.of("§cAre you sure you want to reset §o§nall§c player data?\n§4/kingdom reset all confirm§c to reset §4§o§lALL data").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.reset.all")) {
                                    TeamManager.resetAllData();
                                    BattleKingdom.reload();
                                    Text.of("§aSuccessfully reset all player data.").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "player":
                                if (args.length == 2 && sender.hasPermission("kingdom.reset.player")) {
                                    Text.of("§cYou must specify a player.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.reset.player")) {
                                    String playerName = args[2];
                                    Player p = Bukkit.getPlayer(playerName);
                                    if (p == null) {
                                        Text.of("§cPlayer not found.").send(sender);
                                        return true;
                                    }
                                    UUID playerUUID = p.getUniqueId();
                                    PlayerContainer playerContainer = PlayerHandler.getInstance().getContainer();
                                    ServerPlayer serverPlayer = playerContainer.loadData(playerUUID);

                                    if (serverPlayer.getPlayerName() == null) {
                                        Text.of("§cPlayer not found in database.").send(sender);
                                        return true;
                                    }
                                    TeamManager.resetPlayerData(p);
                                    Text.of("§aSuccessfully reset " + playerName + "'s data.").send(sender);
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                            case "team":
                                if (args.length == 2 && sender.hasPermission("kingdom.reset.team")) {
                                    Text.of("§cYou must specify a team.").send(sender);
                                    return true;
                                } else if (sender.hasPermission("kingdom.reset.team")) {
                                    String team = args[2].toUpperCase();
                                    TeamManager.resetTeamData(team);
                                    BattleKingdom.reload();
                                    return true;
                                } else {
                                    Text.of("§cYou do not have permission to use this command.").send(sender);
                                    return true;
                                }
                        }
                        return true;
                    }
            }
        }
        return true;
    }

    @Override
    public @NotNull List<@NotNull String> tab(@NotNull TabCompleteContext tab) {
        CommandSender sender = tab.sender();
        @NotNull String[] args = tab.args();

        if (sender.hasPermission("kingdom.admin")) {
            if (args.length == 1) {
                return ITabCompleterHelper.tabComplete(args[0], subCommands);
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("set")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("team", "leader", "spawn", "name", "color", "exempt"));
                }
                if (args[0].equalsIgnoreCase("randomteams")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("all", "player"));
                }
                if (args[0].equalsIgnoreCase("info")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("team", "player"));
                }
                if (args[0].equalsIgnoreCase("list")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("team"));
                }
                if (args[0].equalsIgnoreCase("respawn")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("player", "team"));
                }
                if (args[0].equalsIgnoreCase("kick")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("player"));
                }
                if (args[0].equalsIgnoreCase("reset")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("player", "team", "all"));
                }
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("team") || args[1].equalsIgnoreCase("leader") || args[1].equalsIgnoreCase("spawn") || args[1].equalsIgnoreCase("name") || args[1].equalsIgnoreCase("color")) {
                    return ITabCompleterHelper.tabComplete(args[2], TeamManager.getLiteralTeamNames());
                }
                if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("exempt")) {
                    return ITabCompleterHelper.tabComplete(args[2], Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList());
                }
            }
            if (args.length == 4) {
                if (args[1].equalsIgnoreCase("team") || args[1].equalsIgnoreCase("leader")) {
                    return ITabCompleterHelper.tabComplete(args[3], Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList());
                }
                if (args[1].equalsIgnoreCase("exempt")) {
                    return ITabCompleterHelper.tabComplete(args[3], List.of("kick", "bystander"));
                }
            }
            if (args.length == 5) {
                if (args[1].equalsIgnoreCase("exempt") && (args[3].equalsIgnoreCase("kick") || args[3].equalsIgnoreCase("bystander"))) {
                    return ITabCompleterHelper.tabComplete(args[4], List.of("true", "false"));
                }

            }

        }
        else if (!sender.hasPermission("kingdom.admin")) {
            if (args.length == 1) {
                return ITabCompleterHelper.tabComplete(args[0], publicSubCommands);
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("info")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("team", "player"));
                }
                if (args[0].equalsIgnoreCase("list")) {
                    return ITabCompleterHelper.tabComplete(args[1], List.of("team"));
                }
            }
            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("team")) {
                    return ITabCompleterHelper.tabComplete(args[2], TeamManager.getLiteralTeamNames());
                }
                if (args[1].equalsIgnoreCase("player")) {
                    return ITabCompleterHelper.tabComplete(args[2], Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .toList());
                }
            }
        }
        return new ArrayList<>();
    }
}

