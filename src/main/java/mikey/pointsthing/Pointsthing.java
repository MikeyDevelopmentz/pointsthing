package mikey.pointsthing;

import mikey.pointsthing.commands.ArmorBonusCommand;
import mikey.pointsthing.commands.ClearPointsCommand;
import mikey.pointsthing.commands.LeaderboardCommand;
import mikey.pointsthing.commands.PlaceholderAPITestCommand;
import mikey.pointsthing.commands.PlaceholderTestCommand;
import mikey.pointsthing.commands.PointsCommand;
import mikey.pointsthing.commands.ReloadCommand;
import mikey.pointsthing.storage.FileStorage;
import mikey.pointsthing.storage.MySqlStorage;
import mikey.pointsthing.storage.Storage;
import org.bukkit.plugin.java.JavaPlugin;

public final class Pointsthing extends JavaPlugin {
   private PointsManager pointsManager;
   private PlaceholderManager placeholderManager;
   private ConfigManager configManager;
   private ArmorBonusCalculator armorBonusCalculator;
   private Storage storage;
   private boolean placeholderAPIEnabled = false;

   public void onEnable() {
      this.configManager = new ConfigManager(this);
      this.storage = this.createStorage();
      this.pointsManager = new PointsManager(this, this.storage);
      this.placeholderManager = new PlaceholderManager(this.pointsManager, this.configManager);
      this.armorBonusCalculator = new ArmorBonusCalculator(this, this.configManager);
      PointsListener pointsListener = new PointsListener(this, this.pointsManager, this.configManager);
      this.getServer().getPluginManager().registerEvents(pointsListener, this);
      this.getCommand("points").setExecutor(new PointsCommand(this, this.pointsManager, this.configManager));
      this.getCommand("clearpoints").setExecutor(new ClearPointsCommand(this, this.pointsManager, this.configManager));
      this.getCommand("leaderboard").setExecutor(new LeaderboardCommand(this, this.placeholderManager, this.configManager));
      this.getCommand("placeholdertest").setExecutor(new PlaceholderTestCommand(this.placeholderManager));
      this.getCommand("pointsreload").setExecutor(new ReloadCommand(this));
      this.getCommand("armorbonus").setExecutor(new ArmorBonusCommand(this));
      if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
         this.getLogger().info("PlaceholderAPI found! Registering placeholders...");
         (new PointsExpansion(this)).register();
         this.getCommand("papitest").setExecutor(new PlaceholderAPITestCommand(this));
         this.placeholderAPIEnabled = true;
      }

      int cacheDuration = this.configManager.getCacheDuration();
      this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> this.placeholderManager.updateLeaderboardCache(), 1200L, (long)(20 * cacheDuration));
      int autoSaveInterval = this.configManager.getAutoSaveInterval();
      if (autoSaveInterval > 0) {
         this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> this.pointsManager.savePoints(), (long)(20 * autoSaveInterval), (long)(20 * autoSaveInterval));
         if (this.configManager.isDebugEnabled()) {
            this.getLogger().info("Auto-save scheduled every " + autoSaveInterval + " seconds");
         }
      }

      if (this.configManager.isArmorBonusEnabled()) {
         this.getLogger().info("Armor bonus system is enabled");
      } else {
         this.getLogger().info("Armor bonus system is disabled");
      }

      this.getLogger().info("Points system enabled!");
   }

   public void onDisable() {
      if (this.pointsManager != null) {
         this.pointsManager.savePoints();
      }

      if (this.storage != null) {
         this.storage.close();
      }

      this.getLogger().info("Points system disabled!");
   }

   private Storage createStorage() {
      String type = this.configManager.getStorageType();
      if ("mysql".equalsIgnoreCase(type)) {
         MySqlStorage mysql = new MySqlStorage(
               this.configManager.getMysqlHost(),
               this.configManager.getMysqlPort(),
               this.configManager.getMysqlDatabase(),
               this.configManager.getMysqlUsername(),
               this.configManager.getMysqlPassword(),
               this.configManager.getMysqlTable(),
               this.configManager.isMysqlUseSsl());
         try {
            mysql.init();
            this.getLogger().info("Using storage backend: " + mysql.name());
            return mysql;
         } catch (Exception e) {
            this.getLogger().severe("============================================================");
            this.getLogger().severe(" MYSQL CONNECTION FAILED");
            this.getLogger().severe(" reason: " + e.getMessage());
            this.getLogger().severe(" check your storage.mysql settings in config.yml");
            this.getLogger().severe(" falling back to file storage (points.yml) for now");
            this.getLogger().severe("============================================================");
         }
      }
      FileStorage file = new FileStorage(this);
      try {
         file.init();
      } catch (Exception e) {
         this.getLogger().severe("Failed to initialise file storage: " + e.getMessage());
      }
      this.getLogger().info("Using storage backend: " + file.name());
      return file;
   }

   public void reloadPluginConfig() {
      this.configManager.loadConfig();
      this.placeholderManager.updateLeaderboardCache();
      this.getLogger().info("Configuration reloaded!");
   }

   public PlaceholderManager getPlaceholderManager() {
      return this.placeholderManager;
   }

   public PointsManager getPointsManager() {
      return this.pointsManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public ArmorBonusCalculator getArmorBonusCalculator() {
      return this.armorBonusCalculator;
   }

   public boolean isPlaceholderAPIEnabled() {
      return this.placeholderAPIEnabled;
   }
}
