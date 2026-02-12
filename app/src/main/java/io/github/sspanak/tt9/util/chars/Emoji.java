package io.github.sspanak.tt9.util.chars;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.sspanak.tt9.preferences.settings.SettingsStatic;

class Emoji extends Punctuation {
	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static ArrayList<ArrayList<String>> Emoji = new ArrayList<>(Arrays.asList(
		// positive
		new ArrayList<>(Arrays.asList(
			"🙂", "😀", "🤣", "🤓", "😎", "😛", "😉"
		)),
		// negative
		new ArrayList<>(Arrays.asList(
			"🙁", "😢", "😭", "😱", "😲", "😳", "😐", "😠"
		)),
		// hands
		new ArrayList<>(Arrays.asList(
			"👍", "👋", "✌️", "👏", "🖖", "🤘", "🤝", "💪", "👎"
		)),
		// emotions
		new ArrayList<>(Arrays.asList(
			"❤", "🤗", "😍", "😘", "😇", "😈", "🍺", "🎉", "🥱", "🤔", "🥶", "😬"
		))
	));

	final private static Map<String, ArrayList<String>> EmojiGlyphCache = new HashMap<>();

	public static boolean isGraphic(char ch) {
		return !(ch < 256 || Character.isLetterOrDigit(ch) || Character.isAlphabetic(ch));
	}

	@NonNull
	public static ArrayList<String> getEmoji(int level) {
		return getEmoji(level, true);
	}

	@NonNull
	public static ArrayList<String> getEmoji(int level, boolean useSimpleLoading) {
		if (level < 0 || level >= Emoji.size()) {
			return new ArrayList<>();
		}

		String cacheKey = (useSimpleLoading ? "simple:" : "full:") + level;
		ArrayList<String> availableEmoji = EmojiGlyphCache.get(cacheKey);
		if (availableEmoji == null) {
			Paint paint = new Paint();
			availableEmoji = new ArrayList<>();
			for (String emoji : Emoji.get(level)) {
				if (paint.hasGlyph(emoji)) {
					availableEmoji.add(emoji);
				}
			}

			if (availableEmoji.isEmpty()) {
				availableEmoji = new ArrayList<>(TextEmoticons);
			}

			EmojiGlyphCache.put(cacheKey, availableEmoji);
		}

		int max = SettingsStatic.SUGGESTIONS_MAX;
		if (availableEmoji.size() <= max) {
			return new ArrayList<>(availableEmoji);
		}

		return new ArrayList<>(availableEmoji.subList(0, max));
	}

	public static int getMaxEmojiLevel() {
		return Emoji.size();
	}

	public static boolean isBuiltInEmoji(String emoji) {
		for (ArrayList<String> group : Emoji) {
			if (group.contains(emoji)) {
				return true;
			}
		}

		return false;
	}

	public static void invalidateEmojiCache() {
		EmojiGlyphCache.clear();
	}
}
