package mikey.pointsthing;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
   private final Pointsthing plugin;
   private FileConfiguration config;
   private int killPoints;
   private int autoSaveInterval;
   private boolean resetOnDeath;
   private int startingPoints;
   private int maxPoints;
   private boolean armorBonusEnabled;
   private int nakedKillPoints;
   private Map<String, Double> armorMultipliers;
   private int armorPieceBonus;
   private Map<String, Integer> enchantmentBonus;
   private int maxArmorBonus;
   private boolean debugArmorBonus;
   private int maxLeaderboardPlayers;
   private int cacheDuration;
   private String leaderboardTitle;
   private String leaderboardFooter;
   private boolean debugEnabled;
   private String storageType;
   private String mysqlHost;
   private int mysqlPort;
   private String mysqlDatabase;
   private String mysqlUsername;
   private String mysqlPassword;
   private String mysqlTable;
   private boolean mysqlUseSsl;
   private boolean deathPenaltyEnabled;
   private String deathPenaltyMode;
   private int deathPenaltyAmount;
   private double deathPenaltyPercent;
   private int deathPenaltyMinimum;
   private boolean allowDebt;

   public ConfigManager(Pointsthing plugin) {
      this.plugin = plugin;
      this.loadConfig();
   }

   public void loadConfig() {
      this.plugin.saveDefaultConfig();
      this.plugin.reloadConfig();
      this.config = this.plugin.getConfig();
      this.killPoints = this.config.getInt("points.kill-points", 500);
      this.autoSaveInterval = this.config.getInt("points.auto-save", 300);
      this.resetOnDeath = this.config.getBoolean("points.reset-on-death", false);
      this.startingPoints = this.config.getInt("points.starting-points", 0);
      this.maxPoints = this.config.getInt("points.max-points", -1);
      this.armorBonusEnabled = this.config.getBoolean("points.armor-bonus.enabled", true);
      this.nakedKillPoints = this.config.getInt("points.armor-bonus.naked-kill", 0);
      this.armorMultipliers = new HashMap();
      ConfigurationSection multiplierSection = this.config.getConfigurationSection("points.armor-bonus.armor-multipliers");
      if (multiplierSection != null) {
         for(String armorType : multiplierSection.getKeys(false)) {
            this.armorMultipliers.put(armorType.toLowerCase(), multiplierSection.getDouble(armorType));
         }
      }

      if (this.armorMultipliers.isEmpty()) {
         this.armorMultipliers.put("leather", (double)0.5F);
         this.armorMultipliers.put("gold", 0.8);
         this.armorMultipliers.put("chainmail", (double)1.0F);
         this.armorMultipliers.put("iron", (double)1.5F);
         this.armorMultipliers.put("diamond", (double)2.0F);
         this.armorMultipliers.put("netherite", (double)3.0F);
      }

      this.armorPieceBonus = this.config.getInt("points.armor-bonus.armor-piece-bonus", 25);
      this.enchantmentBonus = new HashMap();
      ConfigurationSection enchantSection = this.config.getConfigurationSection("points.armor-bonus.enchantment-bonus");
      if (enchantSection != null) {
         for(String enchant : enchantSection.getKeys(false)) {
            this.enchantmentBonus.put(enchant.toLowerCase(), enchantSection.getInt(enchant));
         }
      }

      this.maxArmorBonus = this.config.getInt("points.armor-bonus.max-bonus", 500);
      this.debugArmorBonus = this.config.getBoolean("points.armor-bonus.debug-bonus", false);
      this.maxLeaderboardPlayers = this.config.getInt("leaderboard.max-players", 10);
      this.cacheDuration = this.config.getInt("leaderboard.cache-duration", 60);
      this.leaderboardTitle = this.colorize(this.config.getString("leaderboard.title", "&e&l===== POINTS LEADERBOARD ====="));
      this.leaderboardFooter = this.colorize(this.config.getString("leaderboard.footer", "&e&l==========================="));
      this.storageType = this.config.getString("storage.type", "file").toLowerCase();
      this.mysqlHost = this.config.getString("storage.mysql.host", "localhost");
      this.mysqlPort = this.config.getInt("storage.mysql.port", 3306);
      this.mysqlDatabase = this.config.getString("storage.mysql.database", "pointsthing");
      this.mysqlUsername = this.config.getString("storage.mysql.username", "root");
      this.mysqlPassword = this.config.getString("storage.mysql.password", "");
      this.mysqlTable = this.config.getString("storage.mysql.table", "player_points");
      this.mysqlUseSsl = this.config.getBoolean("storage.mysql.use-ssl", false);
      this.deathPenaltyEnabled = this.config.getBoolean("points.death-penalty.enabled", false);
      this.deathPenaltyMode = this.config.getString("points.death-penalty.mode", "match").toLowerCase();
      this.deathPenaltyAmount = this.config.getInt("points.death-penalty.amount", 100);
      this.deathPenaltyPercent = this.config.getDouble("points.death-penalty.percent", 10.0);
      this.deathPenaltyMinimum = this.config.getInt("points.death-penalty.minimum-points", 0);
      this.allowDebt = this.config.getBoolean("points.allow-debt", false);
      this.debugEnabled = this.config.getBoolean("debug.enabled", false);
      if (this.debugEnabled) {
         this.plugin.getLogger().info("Configuration loaded successfully");
         this.plugin.getLogger().info("Kill points: " + this.killPoints);
         this.plugin.getLogger().info("Auto-save interval: " + this.autoSaveInterval + " seconds");
         this.plugin.getLogger().info("Armor bonus enabled: " + this.armorBonusEnabled);
      }

   }

   public String formatLeaderboardEntry(int position, String name, int points) {
      ConfigurationSection formatSection = this.config.getConfigurationSection("leaderboard.entry-format");
      String format;
      if (formatSection != null && formatSection.contains(String.valueOf(position))) {
         format = formatSection.getString(String.valueOf(position));
      } else {
         format = formatSection.getString("default", "&f#{position} &e{name} &7- &a{points} points");
      }

      return this.colorize(format.replace("{position}", String.valueOf(position)).replace("{name}", name).replace("{points}", String.valueOf(points)));
   }

   public String getMessage(String path, String... replacements) {
      String message = this.config.getString("messages." + path, "");

      for(int i = 0; i < replacements.length; i += 2) {
         if (i + 1 < replacements.length) {
            message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
         }
      }

      return this.colorize(message);
   }

   private String colorize(String text) {
      return text == null ? "" : ChatColor.translateAlternateColorCodes('&', text);
   }

   public int getKillPoints() {
      return this.killPoints;
   }

   public int getAutoSaveInterval() {
      return this.autoSaveInterval;
   }

   public boolean isResetOnDeath() {
      return this.resetOnDeath;
   }

   public int getStartingPoints() {
      return this.startingPoints;
   }

   public int getMaxPoints() {
      return this.maxPoints;
   }

   public boolean isArmorBonusEnabled() {
      return this.armorBonusEnabled;
   }

   public int getNakedKillPoints() {
      return this.nakedKillPoints;
   }

   public Map<String, Double> getArmorMultipliers() {
      return this.armorMultipliers;
   }

   public int getArmorPieceBonus() {
      return this.armorPieceBonus;
   }

   public Map<String, Integer> getEnchantmentBonus() {
      return this.enchantmentBonus;
   }

   public int getMaxArmorBonus() {
      return this.maxArmorBonus;
   }

   public boolean isDebugArmorBonus() {
      return this.debugArmorBonus;
   }

   public int getMaxLeaderboardPlayers() {
      return this.maxLeaderboardPlayers;
   }

   public int getCacheDuration() {
      return this.cacheDuration;
   }

   public String getLeaderboardTitle() {
      return this.leaderboardTitle;
   }

   public String getLeaderboardFooter() {
      return this.leaderboardFooter;
   }

   public boolean isDebugEnabled() {
      return this.debugEnabled;
   }

   public String getStorageType() {
      return this.storageType;
   }

   public String getMysqlHost() {
      return this.mysqlHost;
   }

   public int getMysqlPort() {
      return this.mysqlPort;
   }

   public String getMysqlDatabase() {
      return this.mysqlDatabase;
   }

   public String getMysqlUsername() {
      return this.mysqlUsername;
   }

   public String getMysqlPassword() {
      return this.mysqlPassword;
   }

   public String getMysqlTable() {
      return this.mysqlTable;
   }

   public boolean isMysqlUseSsl() {
      return this.mysqlUseSsl;
   }

   public boolean isDeathPenaltyEnabled() {
      return this.deathPenaltyEnabled;
   }

   public String getDeathPenaltyMode() {
      return this.deathPenaltyMode;
   }

   public int getDeathPenaltyAmount() {
      return this.deathPenaltyAmount;
   }

   public double getDeathPenaltyPercent() {
      return this.deathPenaltyPercent;
   }

   public int getDeathPenaltyMinimum() {
      return this.deathPenaltyMinimum;
   }

   public boolean isAllowDebt() {
      return this.allowDebt;
   }
}
