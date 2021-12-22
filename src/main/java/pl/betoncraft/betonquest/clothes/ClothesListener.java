package pl.betoncraft.betonquest.clothes;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.sakuragame.eternal.dragoncore.api.ArmourAPI;
import net.sakuragame.eternal.justmessage.screen.ui.conversation.ConversationScreen;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.api.event.ScreenConversationEvent;
import pl.betoncraft.betonquest.compatibility.citizens.CitizensConversation;
import pl.betoncraft.betonquest.conversation.ConversationData;

import java.util.UUID;

public class ClothesListener implements Listener {

    @EventHandler
    public void onRight(NPCRightClickEvent e) {
        Player player = e.getClicker();
        UUID uuid = player.getUniqueId();
        int npcID = e.getNPC().getId();

        Merchant merchant = BetonQuest.getClothesManager().getMerchant(npcID);
        if (merchant == null) return;

        if (merchant.getPrice() < 0) {
            e.setCancelled(true);
            new CitizensConversation(uuid, "conv_merchant_display", e.getNPC().getEntity().getLocation(), e.getNPC());
        }
        else {
            e.setCancelled(true);
            new CitizensConversation(uuid, "conv_merchant_shop", e.getNPC().getEntity().getLocation(), e.getNPC());
        }

        BetonQuest.getClothesManager().getNpcMap().put(uuid, npcID);
    }

    @EventHandler
    public void onConv(ScreenConversationEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String convID = e.getId();

        if (!(convID.equals("conv_merchant_display") || convID.equals("conv_merchant_shop"))) return;

        Merchant merchant = BetonQuest.getClothesManager().getCurrentMerchant(uuid);
        if (merchant == null) return;

        e.setNpcName(merchant.getName());
        e.setResponse(String.join("\n", merchant.getResponse()));
    }

    @EventHandler
    public void onStart(PlayerConversationStartEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        ConversationData data = e.getConversation().getData();
        if (data == null) return;

        int npcID = data.getNpcID();
        if (npcID != -1) return;

        Integer realID = BetonQuest.getClothesManager().getCurrentNpc(uuid);
        if (realID == null) return;
        Entity entity = CitizensAPI.getNPCRegistry().getById(realID).getEntity();

        ConversationScreen.setConvNPC(player, entity.getUniqueId(), data.getModelScale());
    }

    @EventHandler
    public void onSpawn(NPCSpawnEvent e) {
        NPC npc = e.getNPC();
        int id = npc.getId();

        Merchant merchant = BetonQuest.getClothesManager().getMerchant(id);
        if (merchant == null) return;

        ArmourAPI.setEntitySkin(npc.getEntity().getUniqueId(), merchant.getSkin());
    }

}
