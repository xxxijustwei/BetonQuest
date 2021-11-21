package pl.betoncraft.betonquest.commands.sub;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import pl.betoncraft.betonquest.commands.CommandPerms;

public class HelpCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "help";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage("");
        sender.sendMessage("§7 /bq gTags");
        sender.sendMessage("§7 /bq gPoints");
        sender.sendMessage("§7 /bq objective");
        sender.sendMessage("§7 /bq tags");
        sender.sendMessage("§7 /bq points");
        sender.sendMessage("§7 /bq journal");
        sender.sendMessage("§7 /bq purge <player>");
        sender.sendMessage("§7 /bq condition <player> <condition>");
        sender.sendMessage("§7 /bq event <player> <event>");
        sender.sendMessage("§7 /bq reload - reload plugin.");
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
