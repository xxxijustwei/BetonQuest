package pl.betoncraft.betonquest.conversation.sub;

import com.taylorswiftcn.megumi.uifactory.event.comp.UIFCompSubmitEvent;
import com.taylorswiftcn.megumi.uifactory.event.screen.UIFScreenCloseEvent;
import com.taylorswiftcn.megumi.uifactory.generate.ui.UIFactory;
import com.taylorswiftcn.megumi.uifactory.generate.ui.screen.ScreenUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.conversation.Conversation;
import pl.betoncraft.betonquest.conversation.ConversationIO;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.HashMap;
import java.util.UUID;

public class ScreenConvIO implements ConversationIO, Listener {

    private BetonQuest plugin;

    protected int i;
    protected String npcName;
    protected String response;
    protected HashMap<Integer, String> options;
    protected boolean processingLastClick;
    protected boolean allowClose;
    protected boolean switching;

    protected Conversation conv;
    protected Player player;
    protected ScreenUI ui;
    protected Location location;

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
                /*OptionComp optionComp = new OptionComp();
                for (int i = 1; i <= options.size(); i++) {
                    String s = options.get(i);
                    optionComp.addOption(i, s);
                }

                plugin.getConversationUI().open(player, npcName, response, optionComp);*/
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
        }
    }

    @EventHandler
    public void onSubmit(UIFCompSubmitEvent e) {
        Player target = e.getPlayer();
        String screenID = e.getScreenID();

        /*if (!screenID.equals(ConversationUI.screenID)) return;*/

        int index = Integer.parseInt(e.getParams().getParam(0));

        int i = options.size() - index + 1;
        String message = options.get(i);
        if (message == null) return;

        conv.passPlayerAnswer(i);
    }

    @EventHandler
    public void onScreenClose(UIFScreenCloseEvent e) {

        // do something

        if (!e.getPlayer().equals(player)) return;

        if (switching) return;

        if (allowClose) {
            HandlerList.unregisterAll(this);
            return;
        }
        if (conv.isMovementBlock()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(location);
                    UIFactory.open(player, ui);
                }
            }.runTask(plugin);
        }
        else {
            conv.endConversation();
            HandlerList.unregisterAll(this);
        }
    }
}
