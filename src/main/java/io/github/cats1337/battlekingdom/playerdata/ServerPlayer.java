package io.github.cats1337.battlekingdom.playerdata;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

//import static io.github.cats1337.battlekingdom.playerdata.TeamManager.plugin;

@Data
@Accessors(chain = true)
public class ServerPlayer {
    private final UUID uuid;

    @Setter(AccessLevel.PUBLIC)
    private String playerName; // set to player's minecraft username
    private String teamName; // name of the team the player is on (literal)
//    private String teamColor; // set the team colors to be RED, GREEN, BLUE, YELLOW
    private boolean isTeamLeader;
    private boolean teamRespawn;
    private boolean isTeamEliminated;
    private boolean isEliminated;
    private boolean isExemptFromKick;
    private boolean isBystander;

    public ServerPlayer(UUID uuid, String playerName, String teamName) {
        this.uuid = uuid;
        this.playerName = (playerName != null) ? playerName : "";

        // Attempt to get the player's username from Bukkit
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            this.playerName = player.getName();
        }

        this.teamName = (teamName != null) ? teamName : "";
//        this.teamColor = getDefaultTeamColor(this.teamName);
    }

    // Method to get default team color based on team name
//    private String getDefaultTeamColor(String team) {
//        String key = "teams." + team + ".name";
//        return plugin.getConfig().getString(key, "GRAY"); // Default to "GRAY" if the key is not found
//    }

}
