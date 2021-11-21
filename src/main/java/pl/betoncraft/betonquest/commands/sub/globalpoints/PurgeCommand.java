package pl.betoncraft.betonquest.commands.sub.globalpoints;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class PurgeCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "purge";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        BetonQuest.getInstance().getGlobalData().purgePoints();
        MessageUtils.sendMessage(sender, "global_points_purged");
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
