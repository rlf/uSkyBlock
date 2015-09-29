package us.talabrek.ultimateskyblock.command.admin;

import org.bukkit.command.CommandSender;
import us.talabrek.ultimateskyblock.command.common.AbstractUSBCommand;
import us.talabrek.ultimateskyblock.command.common.CompositeUSBCommand;
import us.talabrek.ultimateskyblock.uSkyBlock;
import us.talabrek.ultimateskyblock.util.FormatUtil;
import us.talabrek.ultimateskyblock.util.I18nUtil;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Debug control.
 */
public class DebugCommand extends CompositeUSBCommand {
    public static final Logger log = Logger.getLogger("us.talabrek.ultimateskyblock");
    private static Handler logHandler = null;

    public DebugCommand(final uSkyBlock plugin) {
        super("debug", "usb.admin.debug", I18nUtil.tr("control debugging"));
        add(new AbstractUSBCommand("setlevel", null, "level", I18nUtil.tr("set debug-level")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (args.length == 1) {
                    setLogLevel(sender, args[0]);
                    return true;
                }
                return false;
            }
        });
        add(new AbstractUSBCommand("enable|disable", null, I18nUtil.tr("toggle debug-logging")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (logHandler != null && alias.equals("disable")) {
                    disableLogging(sender);
                } else if (alias.equals("enable")) {
                    enableLogging(sender, plugin);
                } else {
                    sender.sendMessage(I18nUtil.tr("\u00a74Logging wasn't active, so you can't disable it!"));
                }
                return true;
            }
        });
        add(new AbstractUSBCommand("flush", null, I18nUtil.tr("flush current content of the logger to file.")) {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                if (logHandler != null) {
                    logHandler.flush();
                    sender.sendMessage(I18nUtil.tr("\u00a7eLog-file has been flushed."));
                } else {
                    sender.sendMessage(I18nUtil.tr("\u00a74Logging is not enabled, use \u00a7d/usb debug enable"));
                }
                return true;
            }
        });
        String debugLevel = plugin.getConfig().getString("options.advanced.debugLevel", null);
        if (debugLevel != null) {
            setLogLevel(plugin.getServer().getConsoleSender(), debugLevel);
        }
    }

    public static void setLogLevel(CommandSender sender, String arg) {
        try {
            Level level = Level.parse(arg.toUpperCase());
            log.setLevel(level);
            uSkyBlock.getInstance().getLogger().setLevel(level);
            sender.sendMessage("\u00a7eSet debug-level to " + level);
            enableLogging(sender, uSkyBlock.getInstance());
        } catch (Exception e) {
            sender.sendMessage(I18nUtil.tr("\u00a74Invalid argument, try INFO, FINE, FINER, FINEST"));
        }
    }

    public static void disableLogging(CommandSender sender) {
        if (logHandler != null) {
            log.removeHandler(logHandler);
            uSkyBlock.getInstance().getLogger().removeHandler(logHandler);
            logHandler.close();
            if (sender != null) {
                sender.sendMessage(I18nUtil.tr("\u00a7eLogging disabled!"));
            }
        }
        logHandler = null;
    }

    public static void enableLogging(CommandSender sender, uSkyBlock plugin) {
        if (logHandler != null) {
            log.removeHandler(logHandler);
            plugin.getLogger().removeHandler(logHandler);
        }
        File logFolder = new File(plugin.getDataFolder(), "logs");
        logFolder.mkdirs();
        try {
            String logFile = logFolder.toString() + File.separator + "uskyblock.%u.log";
            logHandler = new FileHandler(logFile, true);
            logHandler.setFormatter(new SingleLineFormatter());
            log.addHandler(logHandler);
            plugin.getLogger().addHandler(logHandler);
            Level level = log.getLevel() != null ? log.getLevel() : Level.FINER;
            log.log(level, FormatUtil.stripFormatting(plugin.getVersionInfo(true)));
            sender.sendMessage("\u00a7eLogging to " + logFile);
        } catch (IOException e) {
            log.log(Level.WARNING, "Unable to enable logging", e);
            sender.sendMessage("\u00a74Unable to enable logging: " + e.getMessage());
        }
    }

    public static class SingleLineFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String sourceClassName = record.getSourceClassName();
            sourceClassName = sourceClassName.substring(sourceClassName.lastIndexOf(".")+1);
            try {
                return String.format("%1$d %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS.%2$tL %3$s %4$s %5$s\n",
                        record.getMillis(), new Date(record.getMillis()), sourceClassName,
                        record.getSourceMethodName(),
                        MessageFormat.format(record.getMessage(), record.getParameters()));
            } catch (IllegalArgumentException e) {
                return String.format("%1$d %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS.%2$tL %3$s %4$s %5$s %6$s\n",
                        record.getMillis(), new Date(record.getMillis()), sourceClassName,
                        record.getSourceMethodName(),
                        record.getMessage(),
                        record.getParameters() != null ? Arrays.toString(record.getParameters()) : "");
            }
        }
    }
}
