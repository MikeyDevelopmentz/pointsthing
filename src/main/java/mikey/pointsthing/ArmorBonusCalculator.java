package mikey.pointsthing;

import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArmorBonusCalculator {
   private final ConfigManager configManager;
   private final Pointsthing plugin;

   public ArmorBonusCalculator(Pointsthing plugin, ConfigManager configManager) {
      this.plugin = plugin;
      this.configManager = configManager;
   }

   public int calculateBonus(Player player) {
      if (!this.configManager.isArmorBonusEnabled()) {
         return 0;
      } else {
         ItemStack[] armor = player.getInventory().getArmorContents();
         boolean isNaked = true;

         for(ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) {
               isNaked = false;
               break;
            }
         }

         if (isNaked) {
            return this.configManager.getNakedKillPoints();
         } else {
            int totalBonus = 0;

            for(ItemStack item : armor) {
               if (item != null && item.getType() != Material.AIR) {
                  String armorType = this.getArmorType(item);
                  if (armorType != null) {
                     double multiplier = (Double)this.configManager.getArmorMultipliers().getOrDefault(armorType, (double)1.0F);
                     int armorBonus = (int)((double)this.configManager.getArmorPieceBonus() * multiplier);
                     totalBonus += armorBonus;
                     if (this.configManager.isDebugArmorBonus()) {
                        this.plugin.getLogger().info("Victim's " + armorType + " " + this.getArmorPiece(item) + " worth: " + armorBonus + " points (multiplier: " + multiplier + ")");
                     }
                  }

                  ItemMeta meta = item.getItemMeta();
                  if (meta != null && meta.hasEnchants()) {
                     for(Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                        String enchantName = ((Enchantment)entry.getKey()).getKey().getKey().toLowerCase();
                        int enchantLevel = (Integer)entry.getValue();
                        int enchantBonus = (Integer)this.configManager.getEnchantmentBonus().getOrDefault(enchantName, 0) * enchantLevel;
                        totalBonus += enchantBonus;
                        if (this.configManager.isDebugArmorBonus() && enchantBonus > 0) {
                           this.plugin.getLogger().info("Victim's enchantment " + enchantName + " " + enchantLevel + " worth: " + enchantBonus + " points");
                        }
                     }
                  }
               }
            }

            int maxBonus = this.configManager.getMaxArmorBonus();
            if (maxBonus > 0 && totalBonus > maxBonus) {
               if (this.configManager.isDebugArmorBonus()) {
                  this.plugin.getLogger().info("Bonus points capped from " + totalBonus + " to " + maxBonus);
               }

               totalBonus = maxBonus;
            }

            if (this.configManager.isDebugArmorBonus()) {
               Logger var10000 = this.plugin.getLogger();
               String var10001 = player.getName();
               var10000.info("Total points for killing " + var10001 + " based on armor: " + totalBonus);
            }

            return totalBonus;
         }
      }
   }

   private String getArmorType(ItemStack item) {
      String name = item.getType().name().toLowerCase();
      if (name.contains("leather")) {
         return "leather";
      } else if (!name.contains("golden") && !name.contains("gold")) {
         if (!name.contains("chainmail") && !name.contains("chain")) {
            if (name.contains("iron")) {
               return "iron";
            } else if (name.contains("diamond")) {
               return "diamond";
            } else {
               return name.contains("netherite") ? "netherite" : null;
            }
         } else {
            return "chainmail";
         }
      } else {
         return "gold";
      }
   }

   private String getArmorPiece(ItemStack item) {
      String name = item.getType().name().toLowerCase();
      if (!name.contains("helmet") && !name.contains("cap")) {
         if (!name.contains("chestplate") && !name.contains("tunic")) {
            if (!name.contains("leggings") && !name.contains("pants")) {
               return name.contains("boots") ? "boots" : "unknown";
            } else {
               return "leggings";
            }
         } else {
            return "chestplate";
         }
      } else {
         return "helmet";
      }
   }
}
