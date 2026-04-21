package mikey.pointsthing.commands;

import mikey.pointsthing.ConfigManager;
import mikey.pointsthing.PlaceholderManager;
import mikey.pointsthing.Pointsthing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LeaderboardCommand implements CommandExecutor {
   private final Pointsthing plugin;
   private final PlaceholderManager placeholderManager;
   private final ConfigManager configManager;

   public LeaderboardCommand(Pointsthing plugin, PlaceholderManager placeholderManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.placeholderManager = placeholderManager;
      this.configManager = configManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      this.placeholderManager.updateLeaderboardCache();
      sender.sendMessage(this.placeholderManager.getLeaderboardTitle());
      int maxPlayers = this.configManager.getMaxLeaderboardPlayers();
      int shownPlayers = 0;

      for(int i = 1; i <= maxPlayers; ++i) {
         String formattedEntry = this.placeholderManager.getFormattedLeaderboardEntry(i);
         if (formattedEntry != null) {
            sender.sendMessage(formattedEntry);
            ++shownPlayers;
         }
      }

      if (shownPlayers == 0) {
         sender.sendMessage("§7No players on the leaderboard yet.");
      }

      sender.sendMessage(this.placeholderManager.getLeaderboardFooter());
      return true;
   }
}
