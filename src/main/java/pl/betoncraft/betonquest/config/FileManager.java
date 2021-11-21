package pl.betoncraft.betonquest.config;

import com.taylorswiftcn.justwei.file.JustConfiguration;
import com.taylorswiftcn.justwei.util.MegumiUtil;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.betoncraft.betonquest.BetonQuest;

import java.io.File;

public class FileManager extends JustConfiguration {

    private final BetonQuest plugin;
    @Getter private YamlConfiguration config;
    @Getter private YamlConfiguration message;

    @Getter private static ConfigPackage packages;

    public FileManager(BetonQuest plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void init() {
        config = initFile("config.yml");
        message = initFile("message.yml");

        createPackage();
        loadPackage();
    }

    private void createPackage() {
        File dir = new File(plugin.getDataFolder(), "setting");
        if (!dir.exists()) {
            dir.mkdirs();
            createDefaultAccessor(dir, AccessorType.MAIN);
            createDefaultAccessor(dir, AccessorType.EVENTS);
            createDefaultAccessor(dir, AccessorType.CONDITIONS);
            createDefaultAccessor(dir, AccessorType.JOURNAL);
            createDefaultAccessor(dir, AccessorType.OBJECTIVES);
            createDefaultAccessor(dir, AccessorType.CUSTOM);
            createDefaultAccessor(dir, AccessorType.CONVERSATION);
        }
    }

    private void loadPackage() {
        File file = new File(plugin.getDataFolder(), "setting");
        packages = new ConfigPackage(file);
    }

    private void createDefaultAccessor(File file, AccessorType type) {
        copyResource(file, type.getResource(), type.getPath());
    }

    private void copyResource(File target, String resource, String fileName) {
        File dir = new File(target, fileName);
        if (!dir.exists()) dir.getParentFile().mkdirs();
        MegumiUtil.copyFile(plugin.getResource(resource), dir);
    }

    public static String getConfig(String address) {
        return BetonQuest.getFileManager().getConfig().getString(address);
    }

    public static String getTablePrefix() {
        return getConfig("mysql.prefix");
    }

    public static String getMessage(String address) {
        return getMessage(address, null);
    }

    public static String getMessage(String address, String[] variables) {
        String result = BetonQuest.getFileManager().getMessage().getString(address);

        if (result != null) {
            if (variables != null)
                for (int i = 0; i < variables.length; i++) {
                    result = result.replace("{" + (i + 1) + "}", variables[i]);
                }
            result = result.replace('&', '§');
        }

        return result;
    }

    public static String getNPC(int id) {
        return packages.getNpc().get(id);
    }
}
