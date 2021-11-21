package pl.betoncraft.betonquest.commands.sub.globalpoints;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;

public class HelpCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "help";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage("");
        sender.sendMessage("§7 /bq gpoints list");
        sender.sendMessage("§7 /bq gpoints purge");
        sender.sendMessage("§7 /bq gpoints add <category> <count>");
        sender.sendMessage("§7 /bq gpoints del <category>");
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
