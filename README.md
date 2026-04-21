# pointsthing

small spigot/bukkit plugin i made for tracking player kill points + leaderboard, with optional placeholderapi support and a little armor-based bonus system.

built against spigot 1.19 api.

## building

needs jdk 17.

just open the folder in intellij, it picks up the gradle project automatically. then run the `shadowJar` task from the gradle tab. jar ends up in `build/libs/`.

if you have gradle on your path you can also do:

```
gradle shadowJar
```

or generate the wrapper once with `gradle wrapper` and then use `gradlew` after.

## installing

drop the jar in your servers `plugins/` folder and restart. placeholderapi is optional (softdepend).

## storage

two options, set in `config.yml`:

- `file` - default, saves to `plugins/pointsthing/points.yml`
- `mysql` - saves to a mysql database

for mysql:

```yaml
storage:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: pointsthing
    username: root
    password: "yourpassword"
    table: player_points
    use-ssl: false
```

the table is created on first run so you dont have to make it yourself. if mysql fails to connect it falls back to file storage so the plugin doesnt just die.


## commands

- `/points [player]` - check points
- `/leaderboard` - top players
- `/clearpoints` - wipe everything (op)
- `/pointsreload` - reload config (op)
- `/armorbonus [player]` - preview bonus for killing someone with their current armor
- `/placeholdertest`, `/papitest` - for debugging placeholders
