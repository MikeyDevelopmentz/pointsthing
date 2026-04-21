package mikey.pointsthing.commands;

import mikey.pointsthing.PlaceholderManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlaceholderTestCommand implements CommandExecutor {
   private final PlaceholderManager placeholderManager;

   public PlaceholderTestCommand(PlaceholderManager placeholderManager) {
      this.placeholderManager = placeholderManager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 1) {
         sender.sendMessage("§cUsage: /placeholdertest <placeholder>");
         return true;
      } else {
         String placeholder = args[0];
         String result = this.placeholderManager.processPlaceholders(placeholder);
         sender.sendMessage("§eInput: §f" + placeholder);
         sender.sendMessage("§eResult: §f" + result);
         return true;
      }
   }
}
