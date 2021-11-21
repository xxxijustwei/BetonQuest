package pl.betoncraft.betonquest.commands.sub.points;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import com.taylorswiftcn.justwei.util.MegumiUtil;
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
        if (args.length < 3) return;

        String s = args[0];
        String category = args[1];
        String value = args[2];

        if (!MegumiUtil.isInteger(value)) {
            MessageUtils.sendMessage(sender, "specify_amount");
            return;
        }

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

        data.modifyPoints(category, Integer.parseInt(value));
        MessageUtils.sendMessage(sender, "points_added");
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
