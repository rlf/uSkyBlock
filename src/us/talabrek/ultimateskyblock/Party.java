package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Party implements Serializable {
	private static final long serialVersionUID = 7L;
	private final List<String> members;
	private final SerializableLocation pIsland;
	private String pLeader;
	private int pSize;

	public Party(final String leader, final String member2, final SerializableLocation island) {
		pLeader = leader;
		pSize = 2;
		pIsland = island;
		members = new ArrayList<String>();
		members.add(leader);
		members.add(member2);
	}

	public boolean addMember(final String nMember) {
		if (VaultHandler.checkPerk(pLeader, "usb.extra.partysize", uSkyBlock.getSkyBlockWorld())) {
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

	public boolean changeLeader(final String oLeader, final String nLeader) {
		if (oLeader.equalsIgnoreCase(pLeader)) {
			if (members.contains(nLeader) && !oLeader.equalsIgnoreCase(nLeader)) {
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

	public String getLeader() {
		return pLeader;
	}

	public int getMax() {
		if (VaultHandler.checkPerk(pLeader, "usb.extra.partysize", uSkyBlock.getSkyBlockWorld())) { return Settings.general_maxPartySize * 2; }
		return Settings.general_maxPartySize;
	}

	public List<String> getMembers() {
		final List<String> onlyMembers = members;
		onlyMembers.remove(pLeader);
		return onlyMembers;
	}

	public int getSize() {
		return pSize;
	}

	public boolean hasMember(final String player) {
		if (members.contains(player.toLowerCase())) { return true; }
		if (members.contains(player)) { return true; }
		if (pLeader.equalsIgnoreCase(player)) { return true; }
		return false;
	}

	public int removeMember(final String oMember) {
		if (oMember.equalsIgnoreCase(pLeader)) { return 0; }
		if (members.contains(oMember)) {
			pSize -= 1;
			members.remove(oMember);
			return 2;
		}
		return 1;
	}
}