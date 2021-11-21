package pl.betoncraft.betonquest.commands.sub.globaltags;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.List;

public class ListCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        List<String> tags = BetonQuest.getInstance().getGlobalData().getTags();
        MessageUtils.sendMessage(sender, "global_tags");
        for (String tag : tags) {
            sender.sendMessage("§b- " + tag);
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
