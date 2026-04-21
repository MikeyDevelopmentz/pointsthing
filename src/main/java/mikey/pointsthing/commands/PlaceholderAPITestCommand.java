package mikey.pointsthing.commands;

import mikey.pointsthing.Pointsthing;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PlaceholderAPITestCommand implements CommandExecutor {
   private final Pointsthing plugin;

   public PlaceholderAPITestCommand(Pointsthing plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 1) {
         sender.sendMessage("§cUsage: /papitest <placeholder>");
         return true;
      } else {
         Plugin placeholderAPI = this.plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
         if (placeholderAPI == null) {
            sender.sendMessage("§cPlaceholderAPI is not installed on this server!");
            return true;
         } else {
            String placeholder = args[0];
            if (!(sender instanceof Player)) {
               sender.sendMessage("§cThis command must be run by a player!");
               return true;
            } else {
               Player player = (Player)sender;

               String result;
               try {
                  Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                  result = (String)papiClass.getMethod("setPlaceholders", Player.class, String.class).invoke((Object)null, player, placeholder);
               } catch (Exception e) {
                  sender.sendMessage("§cError processing placeholder: " + e.getMessage());
                  return true;
               }

               sender.sendMessage("§eInput: §f" + placeholder);
               sender.sendMessage("§eResult: §f" + result);
               if (placeholder.startsWith("%points_")) {
                  sender.sendMessage("§e§lPlaceholders from this plugin:");
                  String var10001 = this.getPlaceholderResult(player, "%points_amount%");
                  sender.sendMessage("§e%points_amount%: §f" + var10001);
                  var10001 = this.getPlaceholderResult(player, "%points_top_1_name%");
                  sender.sendMessage("§e%points_top_1_name%: §f" + var10001);
                  var10001 = this.getPlaceholderResult(player, "%points_top_1_points%");
                  sender.sendMessage("§e%points_top_1_points%: §f" + var10001);
               }

               return true;
            }
         }
      }
   }

   private String getPlaceholderResult(Player player, String placeholder) {
      try {
         Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
         return (String)papiClass.getMethod("setPlaceholders", Player.class, String.class).invoke((Object)null, player, placeholder);
      } catch (Exception e) {
         return "Error: " + e.getMessage();
      }
   }
}
