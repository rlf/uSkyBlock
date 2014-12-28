package us.talabrek.ultimateskyblock.command;

import java.util.Comparator;

/**
 * Comparator for sorting sub-commands
 */
public class USBCommandComparator implements Comparator<USBCommand> {
    @Override
    public int compare(USBCommand o1, USBCommand o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
