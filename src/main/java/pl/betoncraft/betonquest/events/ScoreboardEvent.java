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
package pl.betoncraft.betonquest.events;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

/**
 * Adds/removes/multiplies/divides scores on scoreboards.
 *
 * @author Jakub Sapalski
 */
public class ScoreboardEvent extends QuestEvent {

    final VariableNumber count;
    final boolean multi;
    final String objective;

    public ScoreboardEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        persistent = true;
        objective = instruction.next();
        String number = instruction.next();
        if (number.startsWith("*")) {
            multi = true;
            number = number.replace("*", "");
        } else {
            multi = false;
        }
        try {
            count = new VariableNumber(number);
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse score count", e);
        }
    }

    @Override
    public void run(UUID uuid) throws IllegalStateException, QuestRuntimeException {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective(objective);
        if (obj == null) {
            throw new QuestRuntimeException("Scoreboard objective " + objective + " does not exist!");
        }
        Score score = obj.getScore(PlayerConverter.getName(uuid));
        if (multi) {
            score.setScore((int) Math.floor(score.getScore() * count.getDouble(uuid)));
        } else {
            score.setScore((int) Math.floor(score.getScore() + count.getDouble(uuid)));
        }
    }

}
