package pl.betoncraft.betonquest;

import net.sakuragame.eternal.justmessage.JustMessage;
import net.sakuragame.eternal.justmessage.screen.ScreenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.betoncraft.betonquest.commands.*;
import pl.betoncraft.betonquest.compatibility.Compatibility;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.conversation.*;
import pl.betoncraft.betonquest.core.GlobalData;
import pl.betoncraft.betonquest.core.GlobalObjectives;
import pl.betoncraft.betonquest.core.Journal;
import pl.betoncraft.betonquest.core.StaticEvents;
import pl.betoncraft.betonquest.listener.*;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.storage.StorageManager;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;


public class BetonQuest extends JavaPlugin {

    private static BetonQuest instance;

    private FileManager fileManager;
    private StorageManager storageManager;
    private QuestManager questManager;

    private HashMap<UUID, PlayerData> playerData;
    private GlobalData globalData;

    private ScreenManager screenManager;

    @Override
    public void onEnable() {
        instance = this;

        playerData = new HashMap<>();

        LogUtils.setupLogger();

        fileManager = new FileManager(this);
        storageManager = new StorageManager(this);
        fileManager.init();
        storageManager.init();

        questManager = new QuestManager(this);
        questManager.init();

        ConversationColors.init();

        new StaticEvents();
        new GlobalObjectives();

        // schedule quest data loading on the first tick, so all other
        // plugins can register their types
        loadDate();

        screenManager = JustMessage.getInstance().getScreenManager();

        registerListener(new QuestListener());
        registerListener(new PlayerListener());
        registerListener(new MobKillListener());
        registerListener(new CustomDropListener());
        registerListener(new CustomDropListener());
        registerListener(new CombatTagger());
        registerListener(new ConversationListener());

        getCommand("betonquest").setExecutor(new MainCommand());

        Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §6§lBetonQuest succesfully enabled!");
    }

    @Override
    public void onDisable() {
        // suspend all conversations
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
        Compatibility.disable();
        // cancel static events (they are registered outside of Bukkit so it
        // won't happen automatically)
        StaticEvents.stop();
        // done
        LogUtils.getLogger().log(Level.INFO, "BetonQuest succesfully disabled!");
    }


    private void loadDate() {
        Scheduler.runAsync(() -> {
            // Load global tags and points
            globalData = new GlobalData();
            // load data for all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                PlayerData data = storageManager.getPlayerData(uuid);
                playerData.put(uuid, data);
                data.startObjectives();
                data.getJournal().update();
            }
        });
    }

    public static BetonQuest getInstance() {
        return instance;
    }

    public static FileManager getFileManager() {
        return instance.fileManager;
    }

    public static StorageManager getStorageManager() {
        return instance.storageManager;
    }

    public static QuestManager getQuestManager() {
        return instance.questManager;
    }

    public static ScreenManager getScreenManager() {
        return instance.screenManager;
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void reload() {
        // reload the configuration
        LogUtils.getLogger().log(Level.FINE, "Reloading configuration");
        fileManager.init();
        // load new static events
        new StaticEvents();
        // stop current global locations listener
        // and start new one with reloaded configs
        LogUtils.getLogger().log(Level.FINE, "Restarting global locations");
        new GlobalObjectives();
        ConversationColors.init();
        Compatibility.reload();
        // load all events, conditions, objectives, conversations etc.
        questManager.init();
        // start objectives and update journals for every online player
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData playerData = getPlayerData(uuid);
            GlobalObjectives.startAll(uuid);
            Journal journal = playerData.getJournal();
            journal.update();
        }
    }

    public void putPlayerData(UUID uuid, PlayerData playerData) {
        this.playerData.put(uuid, playerData);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public GlobalData getGlobalData() {
        return globalData;
    }

    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
    }
}
