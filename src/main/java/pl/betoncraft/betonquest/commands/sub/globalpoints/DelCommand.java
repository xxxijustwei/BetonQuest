package pl.betoncraft.betonquest.commands.sub.globalpoints;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class DelCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "del";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 1) return;

        String category = args[0];
        BetonQuest.getInstance().getGlobalData().removePointsCategory(category);
        MessageUtils.sendMessage(sender, "points_removed");
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
