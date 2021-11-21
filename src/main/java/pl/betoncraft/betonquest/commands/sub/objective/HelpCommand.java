package pl.betoncraft.betonquest.commands.sub.objective;

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
        sender.sendMessage("§7 /bq objectives list <player>");
        sender.sendMessage("§7 /bq objectives start <player> <objective>");
        sender.sendMessage("§7 /bq objectives delete <player> <objective>");
        sender.sendMessage("§7 /bq objectives complete <player> <objective>");
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
