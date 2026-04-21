package mikey.pointsthing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mikey.pointsthing.storage.Storage;

public class PointsManager {
   private final Pointsthing plugin;
   private final Storage storage;
   private final Map<UUID, Integer> playerPoints = new HashMap<>();

   public PointsManager(Pointsthing plugin, Storage storage) {
      this.plugin = plugin;
      this.storage = storage;
      this.loadFromStorage();
   }

   private void loadFromStorage() {
      try {
         this.storage.loadAll(this.playerPoints);
         if (this.plugin.getConfigManager() != null && this.plugin.getConfigManager().isDebugEnabled()) {
            this.plugin.getLogger().info("Loaded " + this.playerPoints.size() + " players from " + this.storage.name());
         }
      } catch (Exception e) {
         this.plugin.getLogger().severe("Failed to load points from " + this.storage.name() + ": " + e.getMessage());
         e.printStackTrace();
      }
   }

   public void savePoints() {
      try {
         this.storage.saveAll(this.playerPoints);
         if (this.plugin.getConfigManager() != null && this.plugin.getConfigManager().isDebugEnabled()) {
            this.plugin.getLogger().info("Saved " + this.playerPoints.size() + " players to " + this.storage.name());
         }
      } catch (Exception e) {
         this.plugin.getLogger().severe("Failed to save points to " + this.storage.name() + ": " + e.getMessage());
         e.printStackTrace();
      }
   }

   public int getPoints(UUID playerUUID) {
      return this.playerPoints.getOrDefault(playerUUID, 0);
   }

   public int addPoints(UUID playerUUID, int amount) {
      int newTotal = this.getPoints(playerUUID) + amount;
      if (this.plugin.getConfigManager() != null) {
         int maxPoints = this.plugin.getConfigManager().getMaxPoints();
         if (maxPoints > 0 && newTotal > maxPoints) {
            newTotal = maxPoints;
         }
      }
      this.playerPoints.put(playerUUID, newTotal);
      return newTotal;
   }

   public int removePoints(UUID playerUUID, int amount, int minimum) {
      int current = this.getPoints(playerUUID);
      int newTotal = current - amount;
      boolean allowDebt = this.plugin.getConfigManager() != null && this.plugin.getConfigManager().isAllowDebt();
      if (!allowDebt && newTotal < minimum) {
         newTotal = minimum;
      }
      this.playerPoints.put(playerUUID, newTotal);
      return newTotal;
   }

   public void resetPoints(UUID playerUUID) {
      int startingPoints = 0;
      if (this.plugin.getConfigManager() != null) {
         startingPoints = this.plugin.getConfigManager().getStartingPoints();
      }
      this.playerPoints.put(playerUUID, startingPoints);
   }

   public void clearAllPoints() {
      this.playerPoints.clear();
      try {
         this.storage.clearAll();
      } catch (Exception e) {
         this.plugin.getLogger().severe("Failed to clear points in " + this.storage.name() + ": " + e.getMessage());
         e.printStackTrace();
      }
   }

   public Map<UUID, Integer> getAllPlayerPoints() {
      return Collections.unmodifiableMap(this.playerPoints);
   }

   public Storage getStorage() {
      return this.storage;
   }
}
