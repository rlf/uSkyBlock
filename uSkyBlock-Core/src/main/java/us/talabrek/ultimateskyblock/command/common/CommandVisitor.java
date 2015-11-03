package us.talabrek.ultimateskyblock.command.common;

/**
 * Simple visitor for the USBCommands
 */
public interface CommandVisitor {
    void visit(USBCommand cmd);
}
