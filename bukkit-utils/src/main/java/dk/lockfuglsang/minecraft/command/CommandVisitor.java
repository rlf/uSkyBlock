package dk.lockfuglsang.minecraft.command;

/**
 * Simple visitor for the USBCommands
 */
public interface CommandVisitor {
    void visit(Command cmd);
}
