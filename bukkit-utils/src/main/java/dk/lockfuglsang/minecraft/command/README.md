# Commands

This package contains a lot of convenience classes to easily create commands for use with Bukkit.

It has an in-build help system, with pagination, and permission-control.

# Usage

## Getting Started

To create a composite entry-level command, simply inherit from the `AbstractCommandExecutor` in your main plugin.

```java
class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        mycmd = new AbstractCommandExecutor("mycmd", "myplugin.perm.mycmd", "main myplugin command");
        mycmd.add(new AbstractCommand("hello|h", "myplugin.perm.hello", "say hello to the player") {
            @Override
            public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
                sender.sendMessage(new String[]{
                        "Hello! and welcome " + sender.getName(),
                        "I was called with : " + alias,
                        "I had " + args.length + " arguments: " + Arrays.asList(args)
                });
                return true;
            }
        });
        getCommand("mycmd", mycmd);
    }
}
```

The above code, will register 2 commands for the plugin.
One is nested within the other.

Help is automatically generated, so invoking `/mycmd` will show a list of possible options.
Invoking `/mycmd h your momma` will output:

```
Hello! and welcome CONSOLE
I was called with : h
I had 2 arguments: [your, momma]
```

## Tab-Completion

All CompositeCommands have automatic tab-completion of their sub-commands.

If you want to have tab-completion of arguments, you can easily roll your own:

```java
monsterTab = new AbstractTabCompleter() {
  @Override
  protected List<String> getTabList(CommandSender commandSender, String term) {
    return Arrays.asList("animal", "monster", "villager");
  }
}
```
The above tab-completer will do filtering automatically.

Tab-completers can be re-used, if they handle a named argument:

```java
  public class MyCmd extends CompositeCommand {
    public MyCmd() {
      super("mycmd", "perm.mycmd", "main command");
      add(new AbstractCommand("list", "perm.list", "?monster", "lists a subset") { ... });
      add(new AbstractCommand("spawn", "perm.spawn", "monster", "spawn a monster") { ... });
      addTab("monster", monsterTab);
    }
  }
```