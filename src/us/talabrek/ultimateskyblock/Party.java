package us.talabrek.ultimateskyblock;

import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Party implements Serializable {
	private static final long serialVersionUID = 7L;
	private final List<UUID> members;
	private final SerializableLocation pIsland;
	private UUID pLeader;
	private int pSize;

	public Party(final UUID leader, final UUID member2, final SerializableLocation island) {
		pLeader = leader;
		pSize = 2;
		pIsland = island;
		members = new ArrayList<UUID>();
		members.add(leader);
		members.add(member2);
	}

	public boolean addMember(final UUID nMember) {
		if (VaultHandler.checkPerk(Bukkit.getPlayer(pLeader), "usb.extra.partysize", uSkyBlock.getSkyBlockWorld())) {
			if (!members.contains(nMember) && getSize() < Settings.general_maxPartySize * 2) {
				members.add(nMember);
				pSize += 1;
				return true;
			}
			return false;
		}

		if (!members.contains(nMember) && getSize() < Settings.general_maxPartySize) {
			members.add(nMember);
			pSize += 1;
			return true;
		}
		return false;
	}

	public boolean changeLeader(final UUID oLeader, final UUID nLeader) {
		if (oLeader.equals(pLeader)) {
			if (members.contains(nLeader) && !oLeader.equals(nLeader)) {
				pLeader = nLeader;
				members.remove(oLeader);
				members.add(oLeader);
				return true;
			}
		}
		return false;
	}

	public SerializableLocation getIsland() {
		return pIsland;
	}

	public UUID getLeader() {
		return pLeader;
	}

	public int getMax() {
		if (VaultHandler.checkPerk(Bukkit.getPlayer(pLeader), "usb.extra.partysize", uSkyBlock.getSkyBlockWorld())) { return Settings.general_maxPartySize * 2; }
		return Settings.general_maxPartySize;
	}

	public List<UUID> getMembers() {
		final List<UUID> onlyMembers = members;
		onlyMembers.remove(pLeader);
		return onlyMembers;
	}

	public int getSize() {
		return pSize;
	}

	public boolean hasMember(final UUID player) {
		if (members.contains(player)) { return true; }
		if (members.contains(player)) { return true; }
		if (pLeader.equals(player)) { return true; }
		return false;
	}

	public int removeMember(final UUID oMember) {
		if (oMember.equals(pLeader)) { return 0; }
		if (members.contains(oMember)) {
			pSize -= 1;
			members.remove(oMember);
			return 2;
		}
		return 1;
	}
}