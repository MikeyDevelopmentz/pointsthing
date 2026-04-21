package mikey.pointsthing.commands;

import mikey.pointsthing.ConfigManager;
import mikey.pointsthing.PointsManager;
import mikey.pointsthing.Pointsthing;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointsCommand implements CommandExecutor {
   private final Pointsthing plugin;
   private final PointsManager pointsManager;
   private final ConfigManager configManager;

   public PointsCommand(Pointsthing plugin, PointsManager pointsManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.pointsManager = pointsManager;
      this.configManager = configManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
         } else {
            Player player = (Player)sender;
            int points = this.pointsManager.getPoints(player.getUniqueId());
            sender.sendMessage(this.configManager.getMessage("points-self", "points", String.valueOf(points)));
            return true;
         }
      } else if (args.length == 1) {
         Player target = Bukkit.getPlayer(args[0]);
         if (target == null) {
            sender.sendMessage(this.configManager.getMessage("player-not-found"));
            return true;
         } else {
            int points = this.pointsManager.getPoints(target.getUniqueId());
            sender.sendMessage(this.configManager.getMessage("points-other", "player", target.getName(), "points", String.valueOf(points)));
            return true;
         }
      } else {
         sender.sendMessage("§cUsage: /points [player]");
         return true;
      }
   }
}
