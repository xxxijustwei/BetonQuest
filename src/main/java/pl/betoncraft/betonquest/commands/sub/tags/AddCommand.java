package pl.betoncraft.betonquest.commands.sub.tags;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class AddCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "add";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) return;

        String s = args[0];
        String tag = args[1];

        Player player = Bukkit.getPlayerExact(s);
        if (player == null) {
            sender.sendMessage("§c Player's name is missing or he's offline");
            return;
        }

        PlayerData data = BetonQuest.getInstance().getPlayerData(player.getUniqueId());
        if (data == null) {
            sender.sendMessage("§c Player data does not exist");
            return;
        }

        data.addTag(tag);
        MessageUtils.sendMessage(sender, "tag_added");
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String getPermission() {
        return null;
    }
}
