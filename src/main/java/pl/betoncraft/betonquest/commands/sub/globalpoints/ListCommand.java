package pl.betoncraft.betonquest.commands.sub.globalpoints;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
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
        List<Point> points = BetonQuest.getInstance().getGlobalData().getPoints();
        MessageUtils.sendMessage(sender, "global_points");
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
