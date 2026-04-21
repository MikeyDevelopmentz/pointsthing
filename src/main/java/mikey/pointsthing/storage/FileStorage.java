package mikey.pointsthing.storage;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import mikey.pointsthing.Pointsthing;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileStorage implements Storage {
   private final Pointsthing plugin;
   private final File pointsFile;
   private FileConfiguration pointsConfig;

   public FileStorage(Pointsthing plugin) {
      this.plugin = plugin;
      this.pointsFile = new File(plugin.getDataFolder(), "points.yml");
   }

   @Override
   public void init() {
      if (!this.pointsFile.exists()) {
         this.plugin.saveResource("points.yml", false);
      }
      this.pointsConfig = YamlConfiguration.loadConfiguration(this.pointsFile);
   }

   @Override
   public void loadAll(Map<UUID, Integer> out) {
      out.clear();
      if (!this.pointsConfig.contains("players")) {
         return;
      }
      for (String uuidString : this.pointsConfig.getConfigurationSection("players").getKeys(false)) {
         try {
            UUID uuid = UUID.fromString(uuidString);
            int points = this.pointsConfig.getInt("players." + uuidString);
            out.put(uuid, points);
         } catch (IllegalArgumentException ex) {
            this.plugin.getLogger().warning("Invalid UUID in points.yml: " + uuidString);
         }
      }
   }

   @Override
   public void saveAll(Map<UUID, Integer> data) throws java.io.IOException {
      this.pointsConfig.set("players", null);
      for (Map.Entry<UUID, Integer> entry : data.entrySet()) {
         this.pointsConfig.set("players." + entry.getKey().toString(), entry.getValue());
      }
      this.pointsConfig.save(this.pointsFile);
   }

   @Override
   public void clearAll() throws java.io.IOException {
      this.pointsConfig.set("players", null);
      this.pointsConfig.save(this.pointsFile);
   }

   @Override
   public void close() {
      // nothing to close for file storage
      Logger ignored = this.plugin.getLogger();
   }

   @Override
   public String name() {
      return "file (points.yml)";
   }
}
