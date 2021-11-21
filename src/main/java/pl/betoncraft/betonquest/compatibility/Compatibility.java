/*
 * BetonQuest - advanced quests for Bukkit
 * Copyright (C) 2016  Jakub "Co0sh" Sapalski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquest.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.compatibility.citizens.CitizensIntegrator;
import pl.betoncraft.betonquest.compatibility.holographicdisplays.HolographicDisplaysIntegrator;
import pl.betoncraft.betonquest.compatibility.mythicmobs.MythicMobsIntegrator;
import pl.betoncraft.betonquest.compatibility.placeholderapi.PlaceholderAPIIntegrator;
import pl.betoncraft.betonquest.compatibility.protocollib.ProtocolLibIntegrator;
import pl.betoncraft.betonquest.compatibility.vault.VaultIntegrator;
import pl.betoncraft.betonquest.compatibility.worldguard.WorldGuardIntegrator;
import pl.betoncraft.betonquest.exceptions.UnsupportedVersionException;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Compatibility with other plugins
 *
 * @author Jakub Sapalski
 */
public class Compatibility implements Listener {

    private static Compatibility instance;
    private Map<String, Integrator> integrators = new HashMap<>();
    private BetonQuest plugin = BetonQuest.getInstance();
    private List<String> hooked = new ArrayList<>();

    public Compatibility() {
        instance = this;

        integrators.put("MythicMobs", new MythicMobsIntegrator());
        integrators.put("Citizens", new CitizensIntegrator());
        integrators.put("Vault", new VaultIntegrator());
        integrators.put("WorldGuard", new WorldGuardIntegrator());
        integrators.put("PlaceholderAPI", new PlaceholderAPIIntegrator());
        integrators.put("HolographicDisplays", new HolographicDisplaysIntegrator());
        integrators.put("ProtocolLib", new ProtocolLibIntegrator());

        // hook into already enabled plugins in case Bukkit messes up the loading order
        for (Plugin hook : Bukkit.getPluginManager().getPlugins()) {
            hook(hook);
        }

        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());

        // hook into ProtocolLib
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")
                && plugin.getConfig().getString("hook.protocollib").equalsIgnoreCase("true")) {
            hooked.add("ProtocolLib");
        }

        if (hooked.size() > 0) {
            Bukkit.getConsoleSender().sendMessage("§6[BetonQuest] Init hook:");
            for (String plugin : hooked) {
                Bukkit.getConsoleSender().sendMessage("§6[BetonQuest]  §a- " + plugin);
            }
        }
    }

    /**
     * @return the list of hooked plugins
     */
    public static List<String> getHooked() {
        return instance.hooked;
    }

    public static void reload() {
        for (String hooked : getHooked()) {
            instance.integrators.get(hooked).reload();
        }
    }

    public static void disable() {
        for (String hooked : getHooked()) {
            instance.integrators.get(hooked).close();
        }
    }

    private void hook(Plugin hook) {

        // don't want to hook twice
        if (hooked.contains(hook.getName())) {
            return;
        }

        // don't want to hook into disabled plugins
        if (!hook.isEnabled()) {
            return;
        }

        String name = hook.getName();
        Integrator integrator = integrators.get(name);

        // this plugin is not an integration
        if (integrator == null) {
            return;
        }

        // hook into the plugin if it's enabled in the config
        if ("true".equalsIgnoreCase(plugin.getConfig().getString("hook." + name.toLowerCase()))) {

            // log important information in case of an error
            try {
                integrator.hook();
                hooked.add(name);
            } catch (UnsupportedVersionException e) {
                LogUtils.getLogger().log(Level.WARNING, "Could not hook into " + name + ": " +  e.getMessage());
                LogUtils.logThrowable(e);
            } catch (Exception e) {
                LogUtils.getLogger().log(Level.WARNING, String.format("There was an error while hooking into %s %s"
                                + " (BetonQuest %s, Spigot %s).",
                        name, hook.getDescription().getVersion(),
                        plugin.getDescription().getVersion(), Bukkit.getVersion()));
                LogUtils.logThrowableReport(e);
                LogUtils.getLogger().log(Level.WARNING, "BetonQuest will work correctly save for that single integration. "
                        + "You can turn it off by setting 'hook." + name.toLowerCase()
                        + "' to false in config.yml file.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPluginEnable(PluginEnableEvent event) {
        hook(event.getPlugin());
    }

}
