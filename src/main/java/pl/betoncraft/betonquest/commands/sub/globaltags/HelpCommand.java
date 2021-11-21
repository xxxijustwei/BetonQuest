package pl.betoncraft.betonquest.commands.sub.globaltags;

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
        sender.sendMessage("§7 /bq gtags list");
        sender.sendMessage("§7 /bq gtags purge");
        sender.sendMessage("§7 /bq gtags add <tag>");
        sender.sendMessage("§7 /bq gtags del <tag>");
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
