package us.talabrek.ultimateskyblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * This allows sub commands to be handled in a clean easily expandable way.
 * Just create a new command that implements ICommand
 * Then register it with registerCommand() in the static constructor
 * 
 * Try to keep names and aliases in lowercase
 * 
 * @author Schmoller
 *
 */
public class CommandDispatcher implements CommandExecutor, TabCompleter
{
	private String mRootCommandName;
	private String mRootCommandDescription;
	private HashMap<String, ICommand> mCommands;
	
	private ICommand mDefaultCommand = null;
	
	public CommandDispatcher(String commandName, String description)
	{
		mCommands = new HashMap<String, ICommand>();
		
		mRootCommandName = commandName;
		mRootCommandDescription = description;
		
		registerCommand(new InternalHelp());
	}
	/**
	 * Registers a command to be handled by this dispatcher
	 * @param command
	 */
	public void registerCommand(ICommand command)
	{
		mCommands.put(command.getName().toLowerCase(), command);
	}
	
	public void setDefault(ICommand command)
	{
		mDefaultCommand = command;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		if(args.length == 0 && mDefaultCommand == null)
		{
			displayUsage(sender, label, null);
			return true;
		}
		
		ICommand com = null;
		String subCommand = "";
		
		String[] subArgs = args;
		
		if(args.length > 0)
		{
			subCommand = args[0].toLowerCase();
			subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			
			if(mCommands.containsKey(subCommand))
				com = mCommands.get(subCommand);
			else
			{
				// Check aliases
	AliasCheck:	for(Entry<String, ICommand> ent : mCommands.entrySet())
				{
					if(ent.getValue().getAliases() != null)
					{
						String[] aliases = ent.getValue().getAliases();
						for(String alias : aliases)
						{
							if(subCommand.equalsIgnoreCase(alias))
							{
								com = ent.getValue();
								break AliasCheck;
							}
						}
					}
				}
			}
		}
		
		if(com == null)
			com = mDefaultCommand;
		
		// Was not found
		if(com == null)
		{
			displayUsage(sender,label, subCommand);
			return true;
		}
		
		// Check that the sender is correct
		if(!com.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
		{
			if(com == mDefaultCommand)
				displayUsage(sender, label, subCommand);
			else
				sender.sendMessage(ChatColor.RED + "/" + label + " " + subCommand + " cannot be called from the console.");
			return true;
		}
		if(!com.canBeCommandBlock() && sender instanceof BlockCommandSender)
		{
			if(com == mDefaultCommand)
				displayUsage(sender, label, subCommand);
			else
				sender.sendMessage(ChatColor.RED + "/" + label + " " + subCommand + " cannot be called from a command block.");
			return true;
		}
		
		// Check that they have permission
		if(com.getPermission() != null && !sender.hasPermission(com.getPermission()))
		{
			sender.sendMessage(ChatColor.RED + "You do not have permission to use /" + label + " " + subCommand);
			return true;
		}
		
		if(!com.onCommand(sender, subCommand, subArgs))
		{
			sender.sendMessage(ChatColor.RED + "Usage: " + com.getUsageString(subCommand, sender));
		}
		
		return true;
	}
	private void displayUsage(CommandSender sender, String label, String subcommand)
	{
		String usage = "";
		
		boolean first = true;
		boolean odd = true;
		// Build the list
		for(ICommand command : mCommands.values())
		{
			// Check that the sender is correct
			if(!command.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
				continue;
			
			// Check that they have permission
			if(command.getPermission() != null && !sender.hasPermission(command.getPermission()))
				continue;
			
			if(odd)
				usage += ChatColor.WHITE;
			else
				usage += ChatColor.GRAY;
			odd = !odd;
			
			if(first)
				usage += command.getName();
			else
				usage += ", " + command.getName();
			
			first = false;
		}
		
		if(subcommand != null)
			sender.sendMessage(ChatColor.RED + "Unknown command: " + ChatColor.RESET + "/" + label + " " + ChatColor.GOLD + subcommand);
		else
			sender.sendMessage(ChatColor.RED + "No command specified: " + ChatColor.RESET + "/" + label + ChatColor.GOLD + " <command>");

		if(!first)
		{
			sender.sendMessage("Valid commands are:");
			sender.sendMessage(usage);
		}
		else
			sender.sendMessage("There are no commands available to you");
		
		
	}
	
	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args )
	{
		List<String> results = new ArrayList<String>();
		if(args.length == 1) // Tab completing the sub command
		{
			for(ICommand registeredCommand : mCommands.values())
			{
				if(registeredCommand.getName().toLowerCase().startsWith(args[0].toLowerCase()))
				{
					// Check that the sender is correct
					if(!registeredCommand.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
						continue;
					
					// Check that they have permission
					if(registeredCommand.getPermission() != null && !sender.hasPermission(registeredCommand.getPermission()))
						continue;
					
					results.add(registeredCommand.getName());
				}
			}
		}
		else
		{
			// Find the command to use
			String subCommand = args[0].toLowerCase();
			String[] subArgs = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
			
			ICommand com = null;
			if(mCommands.containsKey(subCommand))
			{
				com = mCommands.get(subCommand);
			}
			else
			{
				// Check aliases
	AliasCheck:	for(Entry<String, ICommand> ent : mCommands.entrySet())
				{
					if(ent.getValue().getAliases() != null)
					{
						String[] aliases = ent.getValue().getAliases();
						for(String alias : aliases)
						{
							if(subCommand.equalsIgnoreCase(alias))
							{
								com = ent.getValue();
								break AliasCheck;
							}
						}
					}
				}
			}
			
			// Was not found
			if(com == null)
			{
				return results;
			}
			
			// Check that the sender is correct
			if(!com.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
			{
				return results;
			}
			
			// Check that they have permission
			if(com.getPermission() != null && !sender.hasPermission(com.getPermission()))
			{
				return results;
			}
			
			results = com.onTabComplete(sender, subCommand, subArgs);
			if(results == null)
				return new ArrayList<String>();
		}
		return results;
	}
	
	private class InternalHelp implements ICommand
	{

		@Override
		public String getName()
		{
			return "help";
		}

		@Override
		public String[] getAliases()
		{
			return null;
		}

		@Override
		public String getPermission()
		{
			return null;
		}

		@Override
		public String getUsageString( String label, CommandSender sender )
		{
			return label;
		}

		@Override
		public String getDescription()
		{
			return "Displays this screen.";
		}

		@Override
		public boolean canBeConsole()
		{
			return true;
		}

		@Override
		public boolean canBeCommandBlock()
		{
			return true;
		}

		@Override
		public boolean onCommand( CommandSender sender, String label, String[] args )
		{
			if(args.length != 0)
				return false;
			
			sender.sendMessage(ChatColor.GOLD + mRootCommandDescription);
			sender.sendMessage(ChatColor.GOLD + "Commands: \n");
			
			if(mDefaultCommand != null)
			{
				if((sender instanceof Player || (mDefaultCommand.canBeCommandBlock() && sender instanceof BlockCommandSender) 
						|| (mDefaultCommand.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)))
						&& (mDefaultCommand.getPermission() == null || sender.hasPermission(mDefaultCommand.getPermission())))
				{
					sender.sendMessage(ChatColor.GOLD + "/" + mRootCommandName + " " + mDefaultCommand.getUsageString(mDefaultCommand.getName(), sender));
					
					String[] descriptionLines = mDefaultCommand.getDescription().split("\n");
					for(String line : descriptionLines)
						sender.sendMessage("  " + ChatColor.WHITE + line);
				}
			}
			
			for(ICommand command : mCommands.values())
			{
				// Dont show commands that are irrelevant
				if(!command.canBeCommandBlock() && sender instanceof BlockCommandSender)
					continue;
				if(!command.canBeConsole() && (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender))
					continue;
				
				if(command.getPermission() != null && !sender.hasPermission(command.getPermission()))
					continue;
				
				
				sender.sendMessage(ChatColor.GOLD + "/" + mRootCommandName + " " + command.getUsageString(command.getName(), sender));
				
				String[] descriptionLines = command.getDescription().split("\n");
				for(String line : descriptionLines)
					sender.sendMessage("  " + ChatColor.WHITE + line);
			}
			return true;
		}

		@Override
		public List<String> onTabComplete( CommandSender sender, String label, String[] args )
		{
			return null;
		}
		
	}
}
