package us.talabrek.ultimateskyblock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Party implements Serializable {
	private static final long serialVersionUID = 7L;
	private String pLeader;
	private final SerializableLocation pIsland;
	private int pSize;
	private final List<String> members;

	public Party(String leader, String member2, SerializableLocation island) {
		pLeader = leader;
		pSize = 2;
		pIsland = island;
		members = new ArrayList<String>();
		members.add(leader);
		members.add(member2);
	}

	public String getLeader() {
		return pLeader;
	}

	public SerializableLocation getIsland() {
		return pIsland;
	}

	public int getSize() {
		return pSize;
	}

	public boolean hasMember(String player) {
		if (members.contains(player.toLowerCase()))
			return true;
		if (members.contains(player))
			return true;
		if (pLeader.equalsIgnoreCase(player))
			return true;
		return false;
	}

	public List<String> getMembers() {
		final List<String> onlyMembers = members;
		onlyMembers.remove(pLeader);
		return onlyMembers;
	}

	public boolean changeLeader(String oLeader, String nLeader) {
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

	public int getMax() {
		if (VaultHandler.checkPerk(pLeader, "usb.extra.partysize", uSkyBlock.getSkyBlockWorld())) { return Settings.general_maxPartySize * 2; }
		return Settings.general_maxPartySize;
	}

	public boolean addMember(String nMember) {
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

	public int removeMember(String oMember) {
		if (oMember.equalsIgnoreCase(pLeader)) { return 0; }
		if (members.contains(oMember)) {
			pSize -= 1;
			members.remove(oMember);
			return 2;
		}
		return 1;
	}
}