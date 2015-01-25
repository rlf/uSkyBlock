package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Debug control.
 */
public class DebugCommand extends CompositeUSBCommand {
    private static final Logger log = Logger.getLogger("us.talabrek.ultimateskyblock");
    private static Handler logHandler = null;

    private final uSkyBlock plugin;

    public DebugCommand(final uSkyBlock plugin) {
        super("debug", "usb.admin", "control debugging");
        this.plugin = plugin;
        add(new AbstractUSBCommand("setlevel", null, "level", "set debug-level") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1) {
                    try {
                        Level level = Level.parse(args[0]);
                        log.setLevel(level);
                        sender.sendMessage("\u00a7eSet debug-level to " + level);
                    } catch (Exception e) {
                        sender.sendMessage("\u00a74Invalid argument, try FINE, FINEST, DEBUG, INFO");
                    }
                    return true;
                }
                return false;
            }
        });
        add(new AbstractUSBCommand("enable|disable", null, "toggle debug-logging") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (logHandler != null && alias.equals("disable")) {
                    log.removeHandler(logHandler);
                    logHandler.close();
                    sender.sendMessage("\u00a7eLogging disabled!");
                    logHandler = null;
                } else if (alias.equals("enable")) {
                    if (logHandler != null) {
                        log.removeHandler(logHandler);
                    }
                    File logFolder = new File(plugin.getDataFolder(), "logs");
                    logFolder.mkdirs();
                    try {
                        String logFile = logFolder.toString() + File.separator + "uskyblock.%u.log";
                        logHandler = new FileHandler(logFile, true);
                        logHandler.setFormatter(new SimpleFormatter());
                        log.addHandler(logHandler);
                        sender.sendMessage("\u00a7eLogging to " + logFile);
                    } catch (IOException e) {
                        log.log(Level.WARNING, "Unable to enable logging", e);
                        sender.sendMessage("\u00a74Unable to enable logging: " + e.getMessage());
                    }
                } else {
                    sender.sendMessage("\u00a74Logging wasn't active, so you can't disable it!");
                }
                return true;
            }
        });
        add(new AbstractUSBCommand("flush", null, "flush current content of the logger to file.") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (logHandler != null) {
                    logHandler.flush();
                    sender.sendMessage("\u00a7eLog-file has been flushed.");
                } else {
                    sender.sendMessage("\u00a74Logging is not enabled, use \u00a7d/usb debug enable");
                }
                return true;
            }
        });
    }
}
