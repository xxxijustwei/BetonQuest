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
package pl.betoncraft.betonquest.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.*;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities.
 *
 * @author Jakub Sapalski
 */
public class Utils {

    /**
     * Converts string to list of pages for a book.
     *
     * @param string text to convert
     * @return the list of pages for a book
     */
    public static List<String> pagesFromString(String string) {
        List<String> pages = new ArrayList<>();
        String[] bigPages = string.split("\\|");
        for (String bigPage : bigPages) {
            if (FileManager.getConfig("journal.lines_per_page") != null) {
                final int chars_per_line = Integer.parseInt(FileManager.getConfig("journal.chars_per_line"));
                final int lines_per_page = Integer.parseInt(FileManager.getConfig("journal.lines_per_page"));
                StringBuilder page = new StringBuilder();
                int lines = 0;
                for (String line : bigPage.split("((?<!\\\\)\\\\n|\n)")) {
                    StringBuilder line_builder = new StringBuilder();
                    int line__length = line.replaceAll("[&§][A-Ra-r0-9]", "").replaceAll("((?<!\\\\)\\\\n|\n)", "").length();
                    if (line__length <= chars_per_line) {
                        if (++lines > lines_per_page) {
                            pages.add(page.toString());
                            lines = 1;
                            page = new StringBuilder();
                        }
                        page.append(line).append('\n');
                        continue;
                    }
                    for (String word : line.split(" ")) {
                        int word_length = word.replaceAll("[&§][A-Ra-r0-9]", "").replaceAll("((?<!\\\\)\\\\n|\n)", "").length();
                        if (line_builder.length() + word_length > chars_per_line) {
                            if (++lines > lines_per_page) {
                                pages.add(page.toString());
                                lines = 1;
                                page = new StringBuilder();
                            }
                            page.append(line_builder.toString().trim()).append("\n");
                            line_builder = new StringBuilder();
                        }
                        line_builder.append(word).append(' ');
                    }
                    if (++lines > lines_per_page) {
                        pages.add(page.toString());
                        lines = 1;
                        page = new StringBuilder();
                    }
                    page.append(line_builder.toString().trim()).append('\n');
                }
                if (page.length() != 0) pages.add(page.toString());
            } else {
                final int chars_per_page = Integer.parseInt(FileManager.getConfig("journal.chars_per_page"));
                StringBuilder page = new StringBuilder();
                for (String word : bigPage.split(" ")) {
                    if (page.length() + word.length() + 1 > chars_per_page) {
                        pages.add(page.toString().trim());
                        page = new StringBuilder();
                    }
                    page.append(word).append(" ");
                }
                pages.add(page.toString().trim().replaceAll("(?<!\\\\)\\\\n", "\n"));
            }
        }
        return pages;
    }

    public static List<UUID> getParty(UUID uuid, double range, ConditionID[] conditions) {
        List<UUID> list = new ArrayList<>();
        Player player = PlayerConverter.getPlayer(uuid);
        Location loc = player.getLocation();
        double squared = range * range;
        for (Player otherPlayer : loc.getWorld().getPlayers()) {
            if (otherPlayer.getLocation().distanceSquared(loc) <= squared) {
                boolean meets = true;
                for (ConditionID condition : conditions) {
                    if (!QuestManager.condition(otherPlayer.getUniqueId(), condition)) {
                        meets = false;
                        break;
                    }
                }
                if (meets) {
                    list.add(otherPlayer.getUniqueId());
                }
            }
        }
        return list;
    }

    /**
     * Parses the string as RGB or as DyeColor and returns it as Color.
     *
     * @param string string to parse as a Color
     * @return the Color (never null)
     * @throws InstructionParseException when something goes wrong
     */
    public static Color getColor(String string) throws InstructionParseException {
        if (string == null || string.isEmpty()) {
            throw new InstructionParseException("Color is not specified");
        }
        try {
            return Color.fromRGB(Integer.parseInt(string));
        } catch (NumberFormatException e1) {
            LogUtils.logThrowableIgnore(e1);
            // string is not a decimal number
            try {
                return Color.fromRGB(Integer.parseInt(string.replace("#", ""), 16));
            } catch (NumberFormatException e2) {
                LogUtils.logThrowableIgnore(e2);
                // string is not a hexadecimal number, try dye color
                try {
                    return DyeColor.valueOf(string.trim().toUpperCase().replace(' ', '_')).getColor();
                } catch (IllegalArgumentException e3) {
                    // this was not a dye color name
                    throw new InstructionParseException("Dye color does not exist: " + string, e3);
                }
            }
        } catch (IllegalArgumentException e) {
            // string was a number, but incorrect
            throw new InstructionParseException("Incorrect RGB code: " + string, e);
        }
    }

    /**
     * Resets any color resets to def. Also ensures any new lines copy the colours and format from the previous line
     *
     * @param pages multiple pages to process
     * @param def   default color code to use instead of resetting; use null for regular reset code
     * @return the colorful pages ready to split into multiple lines
     */
    public static List<String> multiLineColorCodes(List<String> pages, String def) {
        String lastCodes = "";
        ListIterator<String> i = pages.listIterator();
        List<String> result = new ArrayList<>();

        while (i.hasNext()) {
            String line = i.next();
            result.add(lastCodes + replaceReset(line, def));
            lastCodes = LocalChatPaginator.getLastColors(line);
        }

        return result;
    }

    /**
     * Replace resets with colorcode
     */
    public static String replaceReset(String string, String color) {
        return string.replace(ChatColor.RESET.toString(), ChatColor.RESET + color);
    }

    /**
     * Formats the string by replacing {@code \\n} with {@code \n} and resolving alternate color codes with {@code &}
     * <p>
     * {@code format(string, false, false)} will return the string with no formatting done
     *
     * @param string     the input string
     * @param colorCodes if alternate color codes should be resolved
     * @param lineBreaks if {@code \\n} should be replaced with {@code \n}
     * @return a formatted version of the input string
     */
    public static String format(String string, boolean colorCodes, boolean lineBreaks) {
        if (colorCodes) string = string.replaceAll("&(?=[A-Ra-r0-9])", "§");
        if (lineBreaks) string = string.replaceAll("(?<!\\\\)\\\\n", "\n");
        return string;
    }

    /**
     * Formats the string by replacing {@code \\n} with {@code \n} and resolving alternate color codes with {@code &}
     *
     * @param string the input string
     * @return a formatted version of the input string
     */
    public static String format(String string) {
        return format(string, true, true);
    }

    /**
     * Split a string by white space, except if between quotes
     */
    public static String[] split(String string) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("(?:(?:(\\S*)(?:\")([^\"]*?)(?:\"))|(\\S+))\\s*").matcher(string);
        while (m.find()) {
            if (m.group(3) != null) {
                list.add(m.group(3));
            } else {
                list.add(m.group(1) + m.group(2));
            }
        }
        return list.toArray(new String[0]);
    }
}
