package pl.betoncraft.betonquest.config;

import com.taylorswiftcn.justwei.file.JustConfiguration;
import com.taylorswiftcn.justwei.util.MegumiUtil;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.clothes.Merchant;

import java.io.File;
import java.util.HashMap;

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
        initClothes();
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

    public void initClothes() {
        File file = new File(plugin.getDataFolder(), "clothes");
        if (!file.exists()) {
            copyResource(file, "clothes/merchant.yml", "merchant.yml");
            copyResource(file, "clothes/conv_merchant_display.yml", "conv_merchant_display.yml");
            copyResource(file, "clothes/conv_merchant_shop.yml", "conv_merchant_shop.yml");
        }

        ConfigAccessor display = new ConfigAccessor(new File(file, "conv_merchant_display.yml"), AccessorType.CONVERSATION);
        ConfigAccessor shop = new ConfigAccessor(new File(file, "conv_merchant_shop.yml"), AccessorType.CONVERSATION);
        packages.addConversations("conv_merchant_display", display);
        packages.addConversations("conv_merchant_shop", shop);

        packages.addEvents("merchant_skin_buy", "buyclothes");
        packages.addEvents("merchant_skin_try", "modelclothes merchant 20");
    }

    public HashMap<Integer, Merchant> getMerchant() {
        File file = new File(plugin.getDataFolder(), "clothes/merchant.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        HashMap<Integer, Merchant> map = new HashMap<>();

        for (String key : yaml.getKeys(false)) {
            map.put(Integer.parseInt(key), new Merchant(yaml.getConfigurationSection(key)));
        }

        return map;
    }
}
