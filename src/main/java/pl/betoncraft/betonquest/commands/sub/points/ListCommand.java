package pl.betoncraft.betonquest.commands.sub.points;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.core.Point;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.List;

public class ListCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) return;

        String s = args[0];

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

        List<Point> points = data.getPoints();
        MessageUtils.sendMessage(sender, "player_points");
        for (Point point : points) {
            sender.sendMessage("§b- " + point.getCategory() + "§e: §a" + point.getCount());
        }
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
