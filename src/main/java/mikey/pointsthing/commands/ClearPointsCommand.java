package mikey.pointsthing.commands;

import mikey.pointsthing.ConfigManager;
import mikey.pointsthing.PointsManager;
import mikey.pointsthing.Pointsthing;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearPointsCommand implements CommandExecutor {
   private final Pointsthing plugin;
   private final PointsManager pointsManager;
   private final ConfigManager configManager;

   public ClearPointsCommand(Pointsthing plugin, PointsManager pointsManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.pointsManager = pointsManager;
      this.configManager = configManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("pointsthing.clearpoints")) {
         sender.sendMessage("§cYou don't have permission to use this command!");
         return true;
      } else {
         this.pointsManager.clearAllPoints();
         sender.sendMessage(this.configManager.getMessage("points-cleared"));
         Bukkit.broadcastMessage("§c§lALL PLAYER POINTS HAVE BEEN RESET!");
         if (this.configManager.isDebugEnabled()) {
            this.plugin.getLogger().info(sender.getName() + " cleared all player points");
         }

         return true;
      }
   }
}
