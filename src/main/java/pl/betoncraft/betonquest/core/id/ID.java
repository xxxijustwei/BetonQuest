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
package pl.betoncraft.betonquest.core.id;

import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;

public abstract class ID {

    public static final String upStr = "_"; // string used as "up the hierarchy" package

    protected String id;
    protected Instruction instruction;
    protected String rawInstruction;

    public ID(String id) throws ObjectNotFoundException {

        // id must be specified
        if (id == null || id.length() == 0) {
            throw new ObjectNotFoundException("ID is null");
        }

        this.id = id;
    }

    public ConfigPackage getPackage() {
        return FileManager.getPackages();
    }

    public String getBaseID() {
        return id;
    }

    public String getFullID() {
        return getBaseID();
    }

    @Override
    public String toString() {
        return getFullID();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ID) {
            ID id = (ID) o;
            return id.id.equals(this.id);
        }
        return false;
    }

    public Instruction generateInstruction() {
        if (rawInstruction == null) {
            return null;
        }
        if (instruction == null) {
            instruction = new Instruction(this, rawInstruction);
        }
        return instruction;
    }

}
