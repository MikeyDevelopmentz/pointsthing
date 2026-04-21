package mikey.pointsthing.storage;

import java.util.Map;
import java.util.UUID;

public interface Storage {

   void init() throws Exception;

   void loadAll(Map<UUID, Integer> out) throws Exception;

   void saveAll(Map<UUID, Integer> data) throws Exception;

   void clearAll() throws Exception;

   void close();

   String name();
}
