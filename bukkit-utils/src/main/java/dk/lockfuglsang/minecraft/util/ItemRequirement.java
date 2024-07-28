package dk.lockfuglsang.minecraft.util;

import org.bukkit.inventory.ItemStack;

// This class should ideally be located in package us.talabrek.ultimateskyblock.challenge. However, it is not possible
// to move there as it is required by ItemStackUtil in this module. This is a limitation of the current design.
// The parsing logic should eventually be moved to the uSkyBlock-Core module.
public record ItemRequirement(ItemStack type, int amount, Operator operator, double increment) {

    public Integer amountForRepetitions(int repetitions) {
        return (int) Math.floor(operator().apply(amount(), increment(), repetitions));
    }

    public enum Operator {
        NONE("", 0.0) {
            @Override
            public double apply(double value, double increment, int repetitions) {
                return value;
            }
        },
        ADD("+", 0.0) {
            @Override
            public double apply(double value, double increment, int repetitions) {
                return value + increment * repetitions;
            }
        },
        SUBTRACT("-", 0.0) {
            @Override
            public double apply(double value, double increment, int repetitions) {
                return value - increment * repetitions;
            }
        },
        MULTIPLY("*", 1.0) {
            @Override
            public double apply(double value, double increment, int repetitions) {
                return value * Math.pow(increment, repetitions);
            }
        },
        DIVIDE("/", 1.0) {
            @Override
            public double apply(double value, double increment, int repetitions) {
                return value / Math.pow(increment, repetitions);
            }
        };

        private final String symbol;
        private final double neutralElement;

        Operator(String symbol, double neutralElement) {
            this.symbol = symbol;
            this.neutralElement = neutralElement;
        }

        public double getNeutralElement() {
            return neutralElement;
        }

        public abstract double apply(double value, double increment, int repetitions);

        public static Operator fromSymbol(String symbol) {
            for (Operator operator : values()) {
                if (operator.symbol.equals(symbol)) {
                    return operator;
                }
            }
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }
    }
}
