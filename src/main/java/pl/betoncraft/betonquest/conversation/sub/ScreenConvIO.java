package pl.betoncraft.betonquest.conversation.sub;

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent;
import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenCloseEvent;
import net.sakuragame.eternal.justmessage.api.MessageAPI;
import net.sakuragame.eternal.justmessage.screen.ui.quest.ConversationScreen;
import net.sakuragame.eternal.justmessage.screen.ui.quest.OptionComp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.event.ScreenConversationEvent;
import pl.betoncraft.betonquest.conversation.Conversation;
import pl.betoncraft.betonquest.conversation.ConversationIO;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.HashMap;
import java.util.UUID;

public class ScreenConvIO implements ConversationIO, Listener {

    private final BetonQuest plugin;

    protected int i;
    protected String npcName;
    protected String response;
    protected HashMap<Integer, String> options;
    protected boolean processingLastClick;
    protected boolean allowClose;
    protected boolean switching;

    protected Conversation conv;
    protected Player player;
    protected Location location;
    protected boolean isOpen;

    public ScreenConvIO(Conversation conv, UUID uuid) {
        this.plugin = BetonQuest.getInstance();
        this.i = 0;
        this.response = null;
        this.options = new HashMap<>();
        this.processingLastClick = false;
        this.allowClose = false;
        this.switching = false;
        this.conv = conv;
        this.player = PlayerConverter.getPlayer(uuid);
        this.location = player.getLocation();
        this.isOpen = false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void setNpcResponse(String npcName, String response) {
        this.npcName = npcName;
        this.response = response;
    }

    @Override
    public void addPlayerOption(String option) {
        this.i++;
        this.options.put(i, option);
    }

    @Override
    public void display() {
        if (conv.isEnded()) return;

        if (response == null) {
            end();
            player.closeInventory();
            return;
        }

        if (options.isEmpty()) {
            end();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                OptionComp optionComp = new OptionComp();
                for (int i = 1; i <= options.size(); i++) {
                    String s = options.get(i);
                    optionComp.addOption(i, s);
                }

                ScreenConversationEvent event = new ScreenConversationEvent(player, conv.getID(), npcName, response);
                event.call();

                BetonQuest.getScreenManager().getConvScreen().open(player, event.getNpcName(), event.getResponse(), optionComp, !isOpen);
                if (!isOpen) {
                    isOpen = true;
                }
                else {
                    switching = true;
                }
            }
        }.runTask(plugin);
    }

    @Override
    public void clear() {
        this.response = null;
        this.options.clear();
        this.i = 0;
    }

    @Override
    public void end() {
        allowClose = true;
        if (response == null && options.isEmpty()) {
            player.closeInventory();
            MessageAPI.setHudVisible(player, true);
        }
    }

    @EventHandler
    public void onSubmit(UIFCompSubmitEvent e) {
        Player target = e.getPlayer();
        String screenID = e.getScreenID();

        if (!screenID.equals(ConversationScreen.screenID)) return;

        int index = e.getParams().getParamI(0);

        String message = options.get(index);
        if (message == null) return;

        conv.passPlayerAnswer(index);
    }

    @EventHandler
    public void onScreenClose(UIFScreenCloseEvent e) {

        // do something

        if (!e.getPlayer().equals(player)) return;

        if (!e.getScreenID().equals(ConversationScreen.screenID)) return;

        if (switching) {
            switching = false;
            return;
        }

        if (allowClose) {
            HandlerList.unregisterAll(this);
            return;
        }
        if (conv.isMovementBlock()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(location);
                }
            }.runTask(plugin);
        }
        else {
            conv.endConversation();
            MessageAPI.setHudVisible(player, true);
            HandlerList.unregisterAll(this);
        }
    }
}
