package pl.betoncraft.betonquest.commands.sub.globaltags;

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

        String tag = args[0];
        BetonQuest.getInstance().getGlobalData().removeTag(tag);
        MessageUtils.sendMessage(sender, "tag_removed");
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
