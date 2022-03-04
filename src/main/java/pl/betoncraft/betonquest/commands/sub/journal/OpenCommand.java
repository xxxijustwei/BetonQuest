package pl.betoncraft.betonquest.commands.sub.journal;

import com.taylorswiftcn.justwei.commands.sub.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;

public class OpenCommand extends SubCommand {
    @Override
    public String getIdentifier() {
        return "open";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Player player = getPlayer();
        BetonQuest.getInstance().getPlayerData(player.getUniqueId()).getJournal().openUI();
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String getPermission() {
        return null;
    }
}
