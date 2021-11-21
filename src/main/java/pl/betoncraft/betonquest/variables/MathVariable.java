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
package pl.betoncraft.betonquest.variables;

import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Variable;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This variable evaluates the given calculation and returns the result.
 *
 * @author Jonas Blocher
 */
public class MathVariable extends Variable {

    private final Calculable calculation;

    public MathVariable(Instruction instruction) throws InstructionParseException {
        super(instruction);
        String instruction_string = instruction.getInstruction();
        if (!instruction_string.matches("math\\.calc:.+")) throw new InstructionParseException("invalid format");
        this.calculation = this.parse(instruction_string.substring("math.calc:".length()));
    }

    @Override
    public String getValue(UUID uuid) {
        try {
            double value = this.calculation.calculate(uuid);
            if (value % 1 == 0)
                return String.format(Locale.US, "%.0f", value);
            return String.valueOf(value);
        } catch (QuestRuntimeException e) {
            LogUtils.getLogger().log(Level.WARNING, "Could not calculate '" + calculation.toString() + "' (" + e.getMessage() + "). Returning 0 instead.");
            LogUtils.logThrowable(e);
            return "0";
        }
    }

    /**
     * Recursively parse a calculable object from the given calculation string which can be calculated in later process
     *
     * @param string the string which should be evaluated
     * @return a calculable object which contains the whole calculation
     * @throws InstructionParseException if the instruction isn't valid
     */
    private Calculable parse(final String string) throws InstructionParseException {
        //clarify error messages for invalid calculations
        if (string.matches(".*[+\\-*/^]{2}.*"))
            throw new InstructionParseException("invalid calculation (operations doubled)");
        if (string.matches(".*(\\([^)]*|\\[[^]]*)")
                || string.matches("([^(]*\\)|[^\\[]*]).*"))
            throw new InstructionParseException("invalid calculation (uneven braces)");
        //calculate braces
        if (string.matches("(\\(.+\\)|\\[.+])")) return this.parse(string.substring(1, string.length() - 1));
        //calculate the absolute value
        if (string.matches("\\|.+\\|")) return new AbsoluteValue(this.parse(string.substring(1, string.length() - 1)));
        String tempCopy = string;
        Matcher m = Pattern.compile("(\\(.+\\)|\\[.+]|\\|.+\\|)").matcher(tempCopy);
        //ignore content of braces for all next operations
        while (m.find()) {
            final int start = m.start();
            final int end = m.end();
            final int length = end - start;
            String s = tempCopy.substring(0, start + 1);
            for (int i = 0; i < length - 2; i++) s += " ";
            s += tempCopy.substring(end - 1);
            tempCopy = s;
        }
        // ADDITION and SUBTRACTION
        int i = tempCopy.lastIndexOf("+");
        int j = tempCopy.lastIndexOf("-");
        if (i > j) {
            if (i == 0)
                return new Calculation(new ClaculableVariable(0), this.parse(string.substring(1)), Operation.ADD);
            //'+' comes after '-'
            return new Calculation(this.parse(string.substring(0, i)),
                    this.parse(string.substring(i + 1)),
                    Operation.ADD);
        } else if (j > i) {
            if (j == 0)
                return new Calculation(new ClaculableVariable(0), this.parse(string.substring(1)), Operation.SUBTRACT);
            //'-' comes after '+'
            return new Calculation(this.parse(string.substring(0, j)),
                    this.parse(string.substring(j + 1)),
                    Operation.SUBTRACT);
        }
        //MULTIPLY and DIVIDE
        i = tempCopy.lastIndexOf("*");
        j = tempCopy.lastIndexOf("/");
        if (i > j) {
            //'*' comes after '/'
            return new Calculation(this.parse(string.substring(0, i)),
                    this.parse(string.substring(i + 1)),
                    Operation.MULTIPLY);
        } else if (j > i) {
            //'/' comes after '*'
            return new Calculation(this.parse(string.substring(0, j)),
                    this.parse(string.substring(j + 1)),
                    Operation.DIVIDE);
        }
        //POW
        i = tempCopy.lastIndexOf("^");
        if (i != -1) {
            return new Calculation(this.parse(string.substring(0, i)),
                    this.parse(string.substring(i + 1)),
                    Operation.POW);
        }
        //if string matches a number
        if (string.matches("\\d+(\\.\\d+)?"))
            return new ClaculableVariable(Double.parseDouble(string));
        //if a variable is specified
        try {
            return new ClaculableVariable("%" + string + "%");
        } catch (NumberFormatException e) {
            throw new InstructionParseException(e.getMessage(), e);
        }
    }

    private enum Operation {
        ADD('+'),
        SUBTRACT('-'),
        MULTIPLY('*'),
        DIVIDE('/'),
        POW('^');

        private final char operator;

        Operation(char operator) {
            this.operator = operator;
        }

        static Operation fromOperator(char operator) {
            for (Operation operation : values()) {
                if (operation.operator == operator) return operation;
            }
            throw new IllegalArgumentException(operator + " is not a supported operator");
        }

        static Operation fromOperator(String operator) {
            if (operator.length() != 1)
                throw new IllegalArgumentException(operator + " is not a supported operator");
            else return Operation.fromOperator(operator.charAt(0));
        }

        public char getOperator() {
            return operator;
        }
    }

    private interface Calculable {

        double calculate(UUID uuid) throws QuestRuntimeException;
    }

    private static class ClaculableVariable implements Calculable {

        private final VariableNumber variable;

        public ClaculableVariable(VariableNumber variable) {
            this.variable = variable;
        }

        public ClaculableVariable(double d) {
            this(new VariableNumber(d));
        }

        public ClaculableVariable(String variable) throws NumberFormatException {
            this(new VariableNumber(variable));
        }

        @Override
        public double calculate(UUID uuid) throws QuestRuntimeException {
            return variable.getDouble(uuid);
        }

        @Override
        public String toString() {
            return variable.toString().replace("%", "");
        }
    }

    private static class AbsoluteValue implements Calculable {

        private final Calculable a;

        public AbsoluteValue(Calculable a) {
            this.a = a;
        }

        @Override
        public String toString() {
            return "|" + a + "|";
        }

        @Override
        public double calculate(UUID uuid) throws QuestRuntimeException {
            return Math.abs(a.calculate(uuid));
        }
    }

    private static class Calculation implements Calculable {

        private final Calculable a;
        private final Calculable b;
        private final Operation operation;

        private Calculation(Calculable a, Calculable b, Operation operation) {
            this.a = a;
            this.b = b;
            this.operation = operation;
        }

        @Override
        public double calculate(UUID uuid) throws QuestRuntimeException {
            try {
                switch (operation) {
                    case ADD:
                        return a.calculate(uuid) + b.calculate(uuid);
                    case SUBTRACT:
                        return a.calculate(uuid) - b.calculate(uuid);
                    case MULTIPLY:
                        return a.calculate(uuid) * b.calculate(uuid);
                    case DIVIDE:
                        return a.calculate(uuid) / b.calculate(uuid);
                    case POW:
                        return Math.pow(a.calculate(uuid), b.calculate(uuid));
                    default:
                        throw new QuestRuntimeException("unsupported operation: " + operation);
                }
            } catch (ArithmeticException e) {
                throw new QuestRuntimeException(e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            String a = ((this.a instanceof Calculation) || this.a.toString().startsWith("-"))
                    ? ("(" + this.a + ")") : this.a.toString();
            String b = ((this.b instanceof Calculation) || this.b.toString().startsWith("-"))
                    ? ("(" + this.b + ")") : this.b.toString();
            return a + operation.operator + b;
        }
    }
}
