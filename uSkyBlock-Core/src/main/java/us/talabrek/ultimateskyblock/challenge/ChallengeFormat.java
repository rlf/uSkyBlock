package us.talabrek.ultimateskyblock.challenge;

import us.talabrek.ultimateskyblock.player.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public final class ChallengeFormat {
    public static String getMissingRequirement(PlayerInfo playerInfo, List<String> requiredChallenges, ChallengeLogic challengeLogic) {
        List<String> missing = new ArrayList<>();
        for (String requiredChallenge : requiredChallenges) {
            String[] split = requiredChallenge.split(":");
            String challengeName = split[0].trim();
            int count = split.length > 1 && split[1].matches("[0-9]+") ? Integer.parseInt(split[1]) : 1;
            if (count < 1) {
                count = 1;
            }
            ChallengeCompletion completion = playerInfo.getChallenge(challengeName);
            if (completion != null && completion.getTimesCompleted() < count) {
                String name = completion.getName();
                missing.add(asDisplayName(name, count - completion.getTimesCompleted(), challengeLogic));
            }
        }
        if (missing.isEmpty()) {
            return null;
        }
        String missingList = "" + missing;
        missingList = missing.toString().substring(1, missingList.length() - 1);
        return missingList;
    }

    private static String asDisplayName(String challengeName, int count, ChallengeLogic challengeLogic) {
        Challenge challenge = challengeLogic.getChallenge(challengeName);
        String displayName = challengeName;
        if (challenge != null) {
            displayName = challenge.getDisplayName();
        }
        if (count > 1) {
            return tr("\u00a7f{0}x \u00a77{1}", count, displayName);
        }
        return tr("\u00a77{0}", displayName);
    }
}
