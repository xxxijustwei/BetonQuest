package pl.betoncraft.betonquest.events;

import net.sakuragame.eternal.gemseconomy.api.GemsEconomyAPI;
import net.sakuragame.eternal.justmessage.api.MessageAPI;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.clothes.Merchant;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class ClothesBuyEvent extends QuestEvent {

    public ClothesBuyEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);

        Merchant merchant = BetonQuest.getClothesManager().getDialogueMerchant(uuid);
        if (merchant == null) return;

        double balance = GemsEconomyAPI.getBalance(uuid, merchant.getCurrency());
        if (balance < merchant.getPrice()) {
            MessageAPI.sendActionTip(player, "&c&l购买失败，你没有足够的" + merchant.getCurrency().getDisplay());
            return;
        }

        GemsEconomyAPI.withdraw(uuid, merchant.getPrice(), merchant.getCurrency());
        int result = merchant.giveItem(player);

        switch (result) {
            case 0:
                player.sendMessage(" &a购买成功！时装已发放到你的背包");
                return;
            case 1:
                player.sendMessage(" &a购买成功！时装已通过邮箱发放，请前往邮箱查收");
        }
    }
}
