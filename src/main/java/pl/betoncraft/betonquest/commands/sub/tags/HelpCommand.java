package pl.betoncraft.betonquest.commands.sub.tags;

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
        sender.sendMessage("§7 /bq tags list <player>");
        sender.sendMessage("§7 /bq tags add <player> <tag>");
        sender.sendMessage("§7 /bq tags del <player> <tag>");
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
