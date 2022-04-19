package pl.betoncraft.betonquest.clothes;

import ink.ptms.adyeshach.api.AdyeshachAPI;
import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent;
import ink.ptms.adyeshach.api.event.AdyeshachPlayerJoinEvent;
import ink.ptms.adyeshach.common.entity.EntityInstance;
import net.sakuragame.eternal.dragoncore.api.ArmourAPI;
import net.sakuragame.eternal.justmessage.screen.ui.quest.ConversationScreen;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.PlayerConversationStartEvent;
import pl.betoncraft.betonquest.api.event.ScreenConversationEvent;
import pl.betoncraft.betonquest.compatibility.adyeshach.AdyeshachConversation;
import pl.betoncraft.betonquest.conversation.ConversationData;
import pl.betoncraft.betonquest.utils.Scheduler;

import java.util.UUID;

public class ClothesListener implements Listener {

//    @EventHandler
//    public void onVisible(AdyeshachPlayerJoinEvent event) {
//        Scheduler.runAsync(new BukkitRunnable() {
//            @Override
//            public void run() {
//                AdyeshachAPI.INSTANCE.getEntityManagerPublic().getEntities().forEach(entity -> {
//                    UUID normalizeUniqueId = entity.getNormalizeUniqueId();
//                    int npcID = Integer.parseInt(entity.getId());
//                    Merchant merchant = BetonQuest.getClothesManager().getMerchant(npcID);
//                    if (merchant == null) {
//                        return;
//                    }
//                    ArmourAPI.setEntitySkin(normalizeUniqueId, merchant.getSkins());
//                });
//            }
//        });
//    }

    @EventHandler
    public void onRight(AdyeshachEntityInteractEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        int npcID = Integer.parseInt(e.getEntity().getId());

        EntityInstance entity = e.getEntity();

        Merchant merchant = BetonQuest.getClothesManager().getMerchant(npcID);
        if (merchant == null) return;

        if (merchant.getPrice() < 0) {
            e.setCancelled(true);
            new AdyeshachConversation(uuid, "conv_merchant_display", entity.getLocation(), entity);
        } else {
            e.setCancelled(true);
            new AdyeshachConversation(uuid, "conv_merchant_shop", entity.getLocation(), entity);
        }

        BetonQuest.getClothesManager().getDialogue().put(uuid, npcID);
    }

    @EventHandler
    public void onConv(ScreenConversationEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String convID = e.getId();

        if (!(convID.equals("conv_merchant_display") || convID.equals("conv_merchant_shop"))) return;

        Merchant merchant = BetonQuest.getClothesManager().getDialogueMerchant(uuid);
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

        Integer realID = BetonQuest.getClothesManager().getDialogueNPC(uuid);
        if (realID == null) return;

        EntityInstance entityFromId = AdyeshachAPI.INSTANCE.getEntityFromId(String.valueOf(realID), player);
        if (entityFromId == null) return;

        ConversationScreen.setConvNPC(player, entityFromId.getNormalizeUniqueId(), data.getModelScale());
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        BetonQuest.getClothesManager().getDialogue().remove(uuid);
        BetonQuest.getClothesManager().getTryMap().remove(uuid);
    }
}
