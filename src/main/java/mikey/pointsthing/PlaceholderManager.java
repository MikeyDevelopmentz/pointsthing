package mikey.pointsthing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlaceholderManager {
   private final PointsManager pointsManager;
   private final ConfigManager configManager;
   private final Pattern topPointsPattern = Pattern.compile("%(\\d+)_top_points%");
   private final Pattern topNamePattern = Pattern.compile("%(\\d+)_top_name%");
   private Map<Integer, String> topPointsCache = new ConcurrentHashMap();
   private Map<Integer, String> topNameCache = new ConcurrentHashMap();
   private long lastCacheUpdate = 0L;

   public PlaceholderManager(PointsManager pointsManager, ConfigManager configManager) {
      this.pointsManager = pointsManager;
      this.configManager = configManager;
   }

   public String processPlaceholders(String text) {
      if (text == null) {
         return "";
      } else {
         int cacheDuration = this.configManager.getCacheDuration();
         if (System.currentTimeMillis() - this.lastCacheUpdate > (long)(cacheDuration * 1000)) {
            this.updateLeaderboardCache();
         }

         Matcher pointsMatcher = this.topPointsPattern.matcher(text);

         while(pointsMatcher.find()) {
            int position = Integer.parseInt(pointsMatcher.group(1));
            if (position >= 1 && position <= 50) {
               String points = (String)this.topPointsCache.getOrDefault(position, "0");
               text = text.replace("%" + position + "_top_points%", points);
            }
         }

         Matcher nameMatcher = this.topNamePattern.matcher(text);

         while(nameMatcher.find()) {
            int position = Integer.parseInt(nameMatcher.group(1));
            if (position >= 1 && position <= 50) {
               String name = (String)this.topNameCache.getOrDefault(position, "None");
               text = text.replace("%" + position + "_top_name%", name);
            }
         }

         return text;
      }
   }

   public void updateLeaderboardCache() {
      List<Map.Entry<UUID, Integer>> leaderboard = this.getLeaderboard();
      this.topPointsCache.clear();
      this.topNameCache.clear();
      int maxPlayers = this.configManager.getMaxLeaderboardPlayers();

      for(int i = 0; i < leaderboard.size() && i < maxPlayers; ++i) {
         Map.Entry<UUID, Integer> entry = (Map.Entry)leaderboard.get(i);
         int position = i + 1;
         UUID uuid = (UUID)entry.getKey();
         String name = this.getPlayerName(uuid);
         this.topPointsCache.put(position, String.valueOf(entry.getValue()));
         this.topNameCache.put(position, name);
      }

      this.lastCacheUpdate = System.currentTimeMillis();
      if (this.configManager.isDebugEnabled()) {
         Bukkit.getLogger().info("Leaderboard cache updated with " + Math.min(leaderboard.size(), maxPlayers) + " players");
      }

   }

   public List<Map.Entry<UUID, Integer>> getLeaderboard() {
      Map<UUID, Integer> playerPoints = this.pointsManager.getAllPlayerPoints();
      List<Map.Entry<UUID, Integer>> leaderboard = new ArrayList(playerPoints.entrySet());
      leaderboard.sort(Entry.comparingByValue(Comparator.reverseOrder()));
      return leaderboard;
   }

   private String getPlayerName(UUID uuid) {
      OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
      return player.getName() != null ? player.getName() : "Unknown";
   }

   public int getPlayerRank(UUID uuid) {
      List<Map.Entry<UUID, Integer>> leaderboard = this.getLeaderboard();

      for(int i = 0; i < leaderboard.size(); ++i) {
         if (((UUID)((Map.Entry)leaderboard.get(i)).getKey()).equals(uuid)) {
            return i + 1;
         }
      }

      return 0;
   }

   public String getFormattedLeaderboardEntry(int position) {
      if (this.topPointsCache.containsKey(position) && this.topNameCache.containsKey(position)) {
         String name = (String)this.topNameCache.get(position);
         int points = Integer.parseInt((String)this.topPointsCache.get(position));
         return this.configManager.formatLeaderboardEntry(position, name, points);
      } else {
         return null;
      }
   }

   public String getLeaderboardTitle() {
      return this.configManager.getLeaderboardTitle();
   }

   public String getLeaderboardFooter() {
      return this.configManager.getLeaderboardFooter();
   }
}
