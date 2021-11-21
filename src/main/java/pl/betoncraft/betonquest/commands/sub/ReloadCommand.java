package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.commands.CommandPerms;
import pl.betoncraft.betonquest.utils.MessageUtils;

public class ReloadCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "reload";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        BetonQuest.getInstance().reload();
        MessageUtils.sendMessage(sender, "reloaded");
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
