package mikey.pointsthing;

import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PointsListener implements Listener {
   private final Pointsthing plugin;
   private final PointsManager pointsManager;
   private final ConfigManager configManager;
   private final ArmorBonusCalculator armorBonusCalculator;

   public PointsListener(Pointsthing plugin, PointsManager pointsManager, ConfigManager configManager) {
      this.plugin = plugin;
      this.pointsManager = pointsManager;
      this.configManager = configManager;
      this.armorBonusCalculator = new ArmorBonusCalculator(plugin, configManager);
   }

   @EventHandler
   public void onPlayerKill(PlayerDeathEvent event) {
      Player killed = event.getEntity();
      Player killer = killed.getKiller();
      if (killer != null) {
         int killPoints = this.configManager.getKillPoints();
         int armorBonus = this.armorBonusCalculator.calculateBonus(killed);
         int totalPointsAwarded = killPoints + armorBonus;
         int newTotal = this.pointsManager.addPoints(killer.getUniqueId(), totalPointsAwarded);
         killer.sendMessage(this.configManager.getMessage("points-received", "points", String.valueOf(totalPointsAwarded), "bonus", String.valueOf(armorBonus), "total", String.valueOf(newTotal)));

         if (this.configManager.isResetOnDeath()) {
            this.pointsManager.resetPoints(killed.getUniqueId());
         } else if (this.configManager.isDeathPenaltyEnabled()) {
            int lost = this.calculatePenalty(killed.getUniqueId(), totalPointsAwarded);
            if (lost > 0) {
               int killedTotal = this.pointsManager.removePoints(killed.getUniqueId(), lost, this.configManager.getDeathPenaltyMinimum());
               killed.sendMessage(this.configManager.getMessage("points-lost", "points", String.valueOf(lost), "total", String.valueOf(killedTotal)));
            }
         }
         if (this.configManager.getAutoSaveInterval() <= 0) {
            this.pointsManager.savePoints();
         }

         if (this.configManager.isDebugEnabled()) {
            Logger var10000 = this.plugin.getLogger();
            String var10001 = killer.getName();
            var10000.info(var10001 + " received " + totalPointsAwarded + " points (base: " + killPoints + ", victim armor bonus: " + armorBonus + ") for killing " + killed.getName() + ". New total: " + newTotal);
         }
      }

   }

   private int calculatePenalty(UUID victim, int killerGained) {
      String mode = this.configManager.getDeathPenaltyMode();
      if ("fixed".equals(mode)) {
         return this.configManager.getDeathPenaltyAmount();
      }
      if ("percent".equals(mode)) {
         int current = this.pointsManager.getPoints(victim);
         return (int) Math.floor(current * (this.configManager.getDeathPenaltyPercent() / 100.0));
      }
      // default: match whatever the killer got
      return killerGained;
   }
}
