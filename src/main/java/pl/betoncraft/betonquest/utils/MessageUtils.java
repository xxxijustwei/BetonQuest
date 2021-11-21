package pl.betoncraft.betonquest.utils;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.notify.Notify;
import pl.betoncraft.betonquest.core.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MessageUtils {

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found).
     *
     * @param uuid    ID of the player
     * @param messageName ID of the message
     */
    public static void sendMessage(UUID uuid, String messageName) {
        sendMessage(uuid, messageName, null, null, null, null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables.
     *
     * @param uuid    ID of the player
     * @param messageName ID of the message
     * @param variables   array of variables which will be inserted into the string
     */
    public static void sendMessage(UUID uuid, String messageName, String[] variables) {
        sendMessage(uuid, messageName, variables, null, null, null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound.
     *
     * @param uuid    ID of the player
     * @param messageName ID of the message
     * @param variables   array of variables which will be inserted into the string
     * @param soundName   name of the sound to play to the player
     */
    public static void sendMessage(UUID uuid, String messageName, String[] variables, String soundName) {
        sendMessage(uuid, messageName, variables, soundName, null, null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound. It will also add a prefix to the message.
     *
     * @param uuid        ID of the player
     * @param messageName     ID of the message
     * @param variables       array of variables which will be inserted into the message
     * @param soundName       name of the sound to play to the player
     * @param prefixName      ID of the prefix
     * @param prefixVariables array of variables which will be inserted into the prefix
     */
    public static void sendMessage(UUID uuid, String messageName, String[] variables, String soundName,
                                   String prefixName, String[] prefixVariables) {
        String message = parseMessage(uuid, messageName, variables, prefixName, prefixVariables);
        if (message == null || message.length() == 0)
            return;

        Player player = PlayerConverter.getPlayer(uuid);
        player.sendMessage(message);
        if (soundName != null) {
            playSound(uuid, soundName);
        }
    }

    public static void sendNotify(UUID uuid, String messageName, String category) {
        sendNotify(uuid, messageName, null, category);
    }

    public static void sendNotify(Player player, String messageName, String category) {
        sendNotify(player, messageName, null, category);
    }

    public static void sendNotify(UUID uuid, String messageName, String[] variables, String category) {
        sendNotify(uuid, messageName, variables, category, null);
    }

    public static void sendNotify(Player player, String messageName, String[] variables, String category) {
        sendNotify(player, messageName, variables, category, null);
    }

    public static void sendNotify(UUID uuid, String messageName, String[] variables, String category, Map<String, String> data) {
        sendNotify(PlayerConverter.getPlayer(uuid), messageName, variables, category, data);
    }

    /**
     * Sends a notification to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound. It will also add a prefix to the message.
     *
     * @param player      player
     * @param messageName ID of the message
     * @param variables   array of variables which will be inserted into the message
     * @param category    notification category
     * @param data        custom notifyIO data
     */
    public static void sendNotify(Player player, String messageName, String[] variables, String category, Map<String, String> data) {
        String message = parseMessage(player, messageName, variables);
        if (message == null || message.length() == 0)
            return;

        Notify.get(category, data).sendNotify(message, player);
    }

    public static String parseMessage(UUID uuid, String messageName, String[] variables) {
        return parseMessage(uuid, messageName, variables, null, null);
    }

    public static String parseMessage(Player player, String messageName, String[] variables) {
        return parseMessage(player, messageName, variables, null, null);
    }

    public static String parseMessage(UUID uuid, String messageName, String[] variables, String prefixName,
                                      String[] prefixVariables) {
        return parseMessage(PlayerConverter.getPlayer(uuid), messageName, variables, prefixName, prefixVariables);
    }

    /**
     * Retrieve's a message in the language of the player, replacing variables
     *
     * @param player          player
     * @param messageName     name of the message to retrieve
     * @param variables       Variables to replace in message
     * @param prefixName      ID of the prefix
     * @param prefixVariables array of variables which will be inserted into the prefix
     */
    public static String parseMessage(Player player, String messageName, String[] variables, String prefixName,
                                      String[] prefixVariables) {
        PlayerData playerData = BetonQuest.getInstance().getPlayerData(player.getUniqueId());
        if (playerData == null)
            return null;
        String message = FileManager.getMessage(messageName, variables);
        if (message == null || message.length() == 0)
            return null;
        if (prefixName != null) {
            String prefix = FileManager.getMessage(prefixName, prefixVariables);
            if (prefix.length() > 0) {
                message = prefix + message;
            }
        }
        return message;
    }

    /**
     * Plays a sound specified in the plugins config to the player
     *
     * @param uuid  the uuid of the player
     * @param soundName the name of the sound to play to the player
     */
    public static void playSound(UUID uuid, String soundName) {
        Player player = PlayerConverter.getPlayer(uuid);
        if (player == null) return;
        String rawSound = BetonQuest.getInstance().getConfig().getString("sounds." + soundName);
        if (!rawSound.equalsIgnoreCase("false")) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(rawSound), 1F, 1F);
            } catch (IllegalArgumentException e) {
                LogUtils.getLogger().log(Level.WARNING, "Unknown sound type: " + rawSound);
                LogUtils.logThrowable(e);
            }
        }
    }

    public static void sendMessage(CommandSender sender, String messageName) {
        sendMessage(sender, messageName, null);
    }

    public static void sendMessage(CommandSender sender, String messageName, String[] variables) {
        messageName = "internal." + messageName;
        if (sender instanceof Player) {
            MessageUtils.sendMessage(((Player) sender).getUniqueId(), messageName, variables);
        } else {
            String message = FileManager.getMessage(messageName, variables);
            sender.sendMessage(message);
        }
    }
}
