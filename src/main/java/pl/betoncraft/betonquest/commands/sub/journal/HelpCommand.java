package pl.betoncraft.betonquest.commands.sub.journal;

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
        sender.sendMessage("§7 /bq journal list <player>");
        sender.sendMessage("§7 /bq journal add <player> <pointer>");
        sender.sendMessage("§7 /bq journal del <player> <pointer>");
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
