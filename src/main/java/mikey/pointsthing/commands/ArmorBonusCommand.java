package mikey.pointsthing.commands;

import mikey.pointsthing.ArmorBonusCalculator;
import mikey.pointsthing.ConfigManager;
import mikey.pointsthing.Pointsthing;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArmorBonusCommand implements CommandExecutor {
   private final Pointsthing plugin;
   private final ConfigManager configManager;
   private final ArmorBonusCalculator armorBonusCalculator;

   public ArmorBonusCommand(Pointsthing plugin) {
      this.plugin = plugin;
      this.configManager = plugin.getConfigManager();
      this.armorBonusCalculator = plugin.getArmorBonusCalculator();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      boolean oldDebug = this.configManager.isDebugArmorBonus();

      try {
         Player target;
         if (args.length == 0) {
            if (!(sender instanceof Player)) {
               sender.sendMessage("§cThis command can only be used by players!");
               boolean var7 = true;
               return var7;
            }

            target = (Player)sender;
         } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
               sender.sendMessage("§cPlayer not found!");
               boolean var15 = true;
               return var15;
            }
         }

         int armorBonus = this.armorBonusCalculator.calculateBonus(target);
         int baseKillPoints = this.configManager.getKillPoints();
         int totalPoints = baseKillPoints + armorBonus;
         if (target == sender) {
            sender.sendMessage("§6Your armor value: §e" + armorBonus + " bonus points");
            sender.sendMessage("§6Killing you would award: §e" + totalPoints + " total points §7(§f" + baseKillPoints + " base §7+ §f" + armorBonus + " bonus§7)");
         } else {
            String var10001 = target.getName();
            sender.sendMessage("§6" + var10001 + "'s armor value: §e" + armorBonus + " bonus points");
            sender.sendMessage("§6Killing them would award: §e" + totalPoints + " total points §7(§f" + baseKillPoints + " base §7+ §f" + armorBonus + " bonus§7)");
         }

         sender.sendMessage("§7Armor status:");
         if (target.getInventory().getHelmet() != null) {
            String var16 = this.formatItemName(target.getInventory().getHelmet().getType().name());
            sender.sendMessage("§7- Helmet: §a" + var16);
         } else {
            sender.sendMessage("§7- Helmet: §cNone");
         }

         if (target.getInventory().getChestplate() != null) {
            String var17 = this.formatItemName(target.getInventory().getChestplate().getType().name());
            sender.sendMessage("§7- Chestplate: §a" + var17);
         } else {
            sender.sendMessage("§7- Chestplate: §cNone");
         }

         if (target.getInventory().getLeggings() != null) {
            String var18 = this.formatItemName(target.getInventory().getLeggings().getType().name());
            sender.sendMessage("§7- Leggings: §a" + var18);
         } else {
            sender.sendMessage("§7- Leggings: §cNone");
         }

         if (target.getInventory().getBoots() != null) {
            String var19 = this.formatItemName(target.getInventory().getBoots().getType().name());
            sender.sendMessage("§7- Boots: §a" + var19);
         } else {
            sender.sendMessage("§7- Boots: §cNone");
         }

         boolean var10 = true;
         return var10;
      } finally {
         if (this.configManager.isDebugArmorBonus() != oldDebug) {
            this.plugin.reloadPluginConfig();
         }

      }
   }

   private String formatItemName(String name) {
      String[] parts = name.toLowerCase().split("_");
      StringBuilder result = new StringBuilder();

      for(String part : parts) {
         if (!part.isEmpty()) {
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
         }
      }

      return result.toString().trim();
   }
}
