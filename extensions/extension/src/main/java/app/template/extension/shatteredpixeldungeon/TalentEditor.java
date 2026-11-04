package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TalentEditor {

	private TalentEditor() {
	}

	static boolean canSwap(int tier) {
		return tier >= 1 && tier <= 3;
	}

	static void addPoint(Hero hero, int tier, Talent talent) {
		int points = hero.pointsInTalent(talent);
		if (points >= talent.maxPoints()) return;
		if (hero.talentPointsAvailable(tier) <= 0) return;
		hero.upgradeTalent(talent);
	}

	static void removePoint(Hero hero, int tier, Talent talent) {
		LinkedHashMap<Talent, Integer> talents = hero.talents.get(tier - 1);
		Integer points = talents.get(talent);
		if (points == null || points <= 0) return;
		talents.put(talent, points - 1);
	}

	static List<Talent> swapOptions(Hero hero, int tier, HeroClass heroClass) {
		List<Talent> options = new ArrayList<>();
		if (!canSwap(tier)) return options;

		ArrayList<LinkedHashMap<Talent, Integer>> classTalents = new ArrayList<>();
		Talent.initClassTalents(heroClass, classTalents);
		if (classTalents.size() < tier) return options;

		LinkedHashMap<Talent, Integer> current = hero.talents.get(tier - 1);
		for (Talent talent : classTalents.get(tier - 1).keySet()) {
			if (!current.containsKey(talent)) options.add(talent);
		}
		return options;
	}

	static void swap(Hero hero, int tier, Talent replacing, Talent replacement) {
		LinkedHashMap<Talent, Integer> current = hero.talents.get(tier - 1);
		if (!current.containsKey(replacing) || current.containsKey(replacement)) return;

		LinkedHashMap<Talent, Integer> updated = new LinkedHashMap<>();
		for (Map.Entry<Talent, Integer> entry : current.entrySet()) {
			Talent key = entry.getKey() == replacing ? replacement : entry.getKey();
			updated.put(key, entry.getValue());
		}

		LinkedHashMap<Talent, Talent> metamorphed = hero.metamorphedTalents;
		if (!metamorphed.containsValue(replacing)) {
			metamorphed.put(replacing, replacement);
		} else if (metamorphed.get(replacement) == replacing) {
			metamorphed.remove(replacement);
		} else {
			for (Talent original : new ArrayList<>(metamorphed.keySet())) {
				if (metamorphed.get(original) == replacing) metamorphed.put(original, replacement);
			}
		}

		hero.talents.set(tier - 1, updated);
		if (hero.hasTalent(replacement)) Talent.onTalentUpgraded(hero, replacement);
	}
}
