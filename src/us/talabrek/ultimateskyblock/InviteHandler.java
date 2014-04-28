package us.talabrek.ultimateskyblock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import us.talabrek.ultimateskyblock.InviteHandler.Invite.Type;
import us.talabrek.ultimateskyblock.async.IslandRemover;

public class InviteHandler {
	private static WeakHashMap<Player, Invite> mInvites = new WeakHashMap<Player, Invite>();
	private static WeakHashMap<Player, HashSet<UUID>> mInvitedPlayers = new WeakHashMap<Player, HashSet<UUID>>();

	public static void invitePlayer(Player player, Player islandOwner) throws IllegalArgumentException {
		UUIDPlayerInfo info = uSkyBlock.getInstance().getPlayer(islandOwner.getUniqueId());

		if (info.getMembers().contains(player.getUniqueId()))
			throw new IllegalArgumentException(player.getName() + " is already in your party.");

		if (uSkyBlock.getInstance().hasParty(player.getUniqueId()))
			throw new IllegalArgumentException(player.getName() + " is already in a party.");

		HashSet<UUID> invites = mInvitedPlayers.get(islandOwner);
		if (invites == null) {
			invites = new HashSet<UUID>();
			mInvitedPlayers.put(islandOwner, invites);
		}

		if (invites.contains(player.getUniqueId()))
			throw new IllegalArgumentException("You have already invited that player");

		// Remove any existing invites
		removeInvite(player);

		invites.add(player.getUniqueId());

		mInvites.put(player, new Invite(islandOwner, Invite.Type.JoinIsland));

		islandOwner.sendMessage("You have invited " + player.getName() + " to join your island.");
		player.sendMessage(islandOwner.getName() + " has invited you to join their island!");
		player.sendMessage("Use " + ChatColor.YELLOW + "/island [accept|reject]" + ChatColor.WHITE + " to accept or reject the invite.");
		// player.sendMessage("This invite will expire in 20 seconds.");
		if (uSkyBlock.getInstance().hasIsland(player.getUniqueId()))
			player.sendMessage(ChatColor.GOLD + "WARNING: You will lose your current island if you accept!");
	}

	public static void transferRequest(Player from, Player to) throws IllegalArgumentException {
		HashSet<UUID> invites = mInvitedPlayers.get(from);
		if (invites == null) {
			invites = new HashSet<UUID>();
			mInvitedPlayers.put(from, invites);
		}

        if (invites.contains(to.getUniqueId()))
            throw new IllegalArgumentException("You have already invited that player");

		// Remove any existing invites
		removeInvite(to);

		invites.add(to.getUniqueId());

		mInvites.put(to, new Invite(from, Invite.Type.Transfer));

        from.sendMessage("You have invited " + to.getName() + " to become your island leader.");
        to.sendMessage(from.getName() + " has invited you to become their island leader!");
        to.sendMessage("Use " + ChatColor.YELLOW + "/island [accept|reject]" + ChatColor.WHITE + " to accept or reject the invite.");
        // player.sendMessage("This invite will expire in 20 seconds.");
        if (uSkyBlock.getInstance().hasIsland(to.getUniqueId()))
            to.sendMessage(ChatColor.GOLD + "WARNING: You will lose your current island if you accept!");
	}

	public static boolean hasInvite(Player player, Invite.Type type) {
		Invite invite = mInvites.get(player);

		return (invite != null && invite.type == type && invite.from.isOnline());
	}

	public static boolean hasInvite(Player player) {
		return mInvites.containsKey(player);
	}

	public static Invite getInvite(Player player) {
		return mInvites.get(player);
	}

	private static void acceptInviteJoin(Invite invite, final Player player) {
		UUIDPlayerInfo info = uSkyBlock.getInstance().getOrCreatePlayer(player.getUniqueId());

		if (info.getHasParty()) {
			player.sendMessage(ChatColor.RED + "You are already part of a party. Please leave the party first.");
			removeInvite(player);
			return;
		}

		if (info.getHasIsland()) {
			IslandRemover remover = new IslandRemover(Arrays.asList(info));
			remover.then(new Runnable() {
				@Override
				public void run() {
					acceptInvite(player);
				}
			});
			remover.start();
			return;
		}

		UUIDPlayerInfo inviterIsland = uSkyBlock.getInstance().getPlayer(invite.from.getUniqueId());
		addPlayerToParty(info, inviterIsland);

		player.sendMessage(ChatColor.GREEN + "You have joined " + invite.from.getName() + "'s island.");
		invite.from.sendMessage(ChatColor.GREEN + player.getName() + " has accepted your invitation to join your island.");

		removeInvite(player);

		if (!Misc.safeTeleport(player, inviterIsland.getTeleportLocation())) {
			// Take them back to spawn
			if (uSkyBlock.isSkyBlockWorld(player.getWorld()))
				Misc.safeTeleport(player, Bukkit.getWorlds().get(0).getSpawnLocation());

			return;
		}

		if (Settings.island_protectWithWorldGuard && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			if (WorldGuardHandler.getWorldGuard().getRegionManager(uSkyBlock.getSkyBlockWorld()).hasRegion(invite.from.getName() + "Island"))
				WorldGuardHandler.addPlayerToOldRegion(invite.from.getName(), player.getName());
		}

	}

	private static void acceptInviteTransfer(Invite invite, final Player player) {
		UUIDPlayerInfo info = uSkyBlock.getInstance().getOrCreatePlayer(player.getUniqueId());

		if (info.getHasParty() && info.getPartyLeader().equals(invite.from.getUniqueId())) {
			info.setLeaveParty();
		}

		if (info.getHasParty()) {
			player.sendMessage(ChatColor.RED + "You are already part of a different party. Please leave the party first.");
			removeInvite(player);
			return;
		}

		if (info.getHasIsland()) {
			IslandRemover remover = new IslandRemover(Arrays.asList(info));
			remover.then(new Runnable() {
				@Override
				public void run() {
					acceptInvite(player);
				}
			});
			remover.start();
			return;
		}

		UUIDPlayerInfo inviterIsland = uSkyBlock.getInstance().getPlayer(invite.from.getUniqueId());

		info.setHasIsland(true);
		info.setIslandLocation(inviterIsland.getIslandLocation());
		info.setIslandLevel(inviterIsland.getIslandLevel());
		info.setPartyIslandLocation(null);

        addPlayerToParty(inviterIsland, info);

		inviterIsland.setHasIsland(false);
		inviterIsland.setIslandLocation(null);
		inviterIsland.setIslandLevel(0);

		info.setHomeLocation(null);
		info.setWarpLocation(null);

		info.getBanned().clear();

		if (Settings.island_protectWithWorldGuard && Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
			WorldGuardHandler.transferRegion(inviterIsland.getPlayer().getName(), info.getPlayer().getName());

		uSkyBlock.getInstance().savePlayer(inviterIsland);
		uSkyBlock.getInstance().savePlayer(info);

		player.sendMessage(ChatColor.GREEN + "You now own " + invite.from.getName() + "'s island.");
		invite.from.sendMessage(ChatColor.GREEN + player.getName() + " has accepted your transfer request.");

		removeInvite(player);
	}

	public static void acceptInvite(final Player player) {
		Invite invite = mInvites.get(player);

		if (invite.type == Type.JoinIsland)
			acceptInviteJoin(invite, player);
		else if (invite.type == Type.Transfer)
			acceptInviteTransfer(invite, player);
		else
			removeInvite(player);
	}

	public static void rejectInvite(Player player) {
		Invite invite = mInvites.get(player);

		switch (invite.type) {
			case JoinIsland:
				player.sendMessage(ChatColor.YELLOW + "You have rejected the invitation to join an island.");
				invite.from.sendMessage(ChatColor.RED + player.getName() + " has rejected your island invite!");
			break;
			case Transfer:
				player.sendMessage(ChatColor.YELLOW + "You have rejected the the transfer request.");
				invite.from.sendMessage(ChatColor.RED + player.getName() + " has rejected your transfer request!");
			break;
		}

		removeInvite(player);
	}

	private static void removeInvite(Player player) {
		Invite invite = mInvites.remove(player);
		if (invite == null)
			return;

		HashSet<UUID> invites = mInvitedPlayers.get(invite.from);
		invites.remove(player.getUniqueId());

		if (invites.isEmpty())
			mInvitedPlayers.remove(invite.from);
	}

	private static void addPlayerToParty(UUIDPlayerInfo player, UUIDPlayerInfo partyLeader) {
		uSkyBlock.getLog().info("Adding " + player.getPlayer().getName() + " to " + partyLeader.getPlayer().getName() + "'s island.");

		if (!partyLeader.getHasParty()){
			partyLeader.setJoinParty(partyLeader.getPlayerUUID(), partyLeader.getIslandLocation());
		}

		player.setJoinParty(partyLeader.getPlayerUUID(), partyLeader.getIslandLocation());

		if (partyLeader.getHomeLocation() != null)
			player.setHomeLocation(partyLeader.getHomeLocation());
		else
			player.setHomeLocation(partyLeader.getIslandLocation());

		partyLeader.addMember(player.getPlayerUUID());
	}

	public static class Invite {
		public enum Type {
			JoinIsland, Transfer
		}

		public Invite(Player from, Type type) {
			this.from = from;
			this.type = type;
		}

		public Player from;
		public Type type;
	}
}
