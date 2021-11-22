package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import net.sakuragame.serversystems.manage.client.api.ClientManagerAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.UUID;

public class PurgeCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "purge";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) return;

        String s = args[0];

        Player player = Bukkit.getPlayerExact(s);
        if (player == null) {
            UUID uuid = ClientManagerAPI.getUserUUID(s);
            Scheduler.runAsync(() -> BetonQuest.getStorageManager().clearPlayerDate(uuid));
            MessageUtils.sendMessage(sender, "purged", new String[]{args[0]});
            return;
        }

        PlayerData data = BetonQuest.getInstance().getPlayerData(player.getUniqueId());
        if (data == null) {
            sender.sendMessage("§c Player data does not exist");
            return;
        }

        data.purgePlayer();
        MessageUtils.sendMessage(sender, "purged", new String[]{args[0]});
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermission() {
        return CommandPerms.ADMIN.getNode();
    }
}
