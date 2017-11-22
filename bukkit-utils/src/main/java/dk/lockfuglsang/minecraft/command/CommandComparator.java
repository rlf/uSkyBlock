package dk.lockfuglsang.minecraft.command;

import java.util.Comparator;

/**
 * Comparator for sorting sub-commands
 */
public class CommandComparator implements Comparator<Command> {
    @Override
    public int compare(Command o1, Command o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
