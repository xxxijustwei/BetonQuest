package pl.betoncraft.betonquest.commands.sub.globalpoints;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import com.taylorswiftcn.justwei.util.MegumiUtil;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class AddCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "add";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) return;

        String category = args[0];
        String value = args[1];

        if (!MegumiUtil.isInteger(value)) {
            MessageUtils.sendMessage(sender, "specify_amount");
            return;
        }

        BetonQuest.getInstance().getGlobalData().modifyPoints(category, Integer.parseInt(value));
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
