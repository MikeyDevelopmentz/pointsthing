package mikey.pointsthing;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PointsExpansion extends PlaceholderExpansion {
   private final Pointsthing plugin;
   private final PointsManager pointsManager;
   private final PlaceholderManager placeholderManager;
   private final ArmorBonusCalculator armorBonusCalculator;

   public PointsExpansion(Pointsthing plugin) {
      this.plugin = plugin;
      this.pointsManager = plugin.getPointsManager();
      this.placeholderManager = plugin.getPlaceholderManager();
      this.armorBonusCalculator = plugin.getArmorBonusCalculator();
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String getAuthor() {
      return this.plugin.getDescription().getAuthors().toString();
   }

   public String getIdentifier() {
      return "points";
   }

   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public String onRequest(OfflinePlayer player, String identifier) {
      if (player == null) {
         return "";
      } else if (identifier.equals("amount")) {
         return String.valueOf(this.pointsManager.getPoints(player.getUniqueId()));
      } else if (identifier.equals("rank")) {
         return String.valueOf(this.placeholderManager.getPlayerRank(player.getUniqueId()));
      } else if (identifier.equals("armor_value") && player.isOnline()) {
         return String.valueOf(this.armorBonusCalculator.calculateBonus(player.getPlayer()));
      } else if (identifier.equals("armor_bonus") && player.isOnline()) {
         return String.valueOf(this.armorBonusCalculator.calculateBonus(player.getPlayer()));
      } else if (identifier.equals("kill_value") && player.isOnline()) {
         int basePoints = this.plugin.getConfigManager().getKillPoints();
         int armorBonus = this.armorBonusCalculator.calculateBonus(player.getPlayer());
         return String.valueOf(basePoints + armorBonus);
      } else if (identifier.equals("armor_bonus_enabled")) {
         return String.valueOf(this.plugin.getConfigManager().isArmorBonusEnabled());
      } else {
         if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");
            if (parts.length < 3) {
               return null;
            }

            int position;
            try {
               position = Integer.parseInt(parts[1]);
               if (position < 1 || position > 50) {
                  return "Invalid position";
               }
            } catch (NumberFormatException var6) {
               return "Invalid number";
            }

            if (parts[2].equals("name")) {
               return this.placeholderManager.processPlaceholders("%" + position + "_top_name%");
            }

            if (parts[2].equals("points")) {
               return this.placeholderManager.processPlaceholders("%" + position + "_top_points%");
            }
         }

         return null;
      }
   }
}
