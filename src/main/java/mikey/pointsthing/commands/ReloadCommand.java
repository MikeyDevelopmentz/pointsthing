package mikey.pointsthing.commands;

import mikey.pointsthing.Pointsthing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
   private final Pointsthing plugin;

   public ReloadCommand(Pointsthing plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      this.plugin.reloadPluginConfig();
      sender.sendMessage("§aConfiguration reloaded successfully!");
      return true;
   }
}
