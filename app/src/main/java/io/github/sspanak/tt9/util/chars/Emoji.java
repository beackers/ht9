package io.github.sspanak.tt9.util.chars;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Arrays;

class Emoji extends Punctuation {
	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	public static boolean isGraphic(char ch) {
		return !(ch < 256 || Character.isLetterOrDigit(ch) || Character.isAlphabetic(ch));
	}

	public static ArrayList<String> getEmoji(int level) {
		if (level < 0 || level >= EmojiDataGenerated.GROUPS.length) {
			return new ArrayList<>();
		}

		Paint paint = new Paint();
		ArrayList<String> availableEmoji = new ArrayList<>();
		for (String emoji : EmojiDataGenerated.GROUPS[level]) {
			if (paint.hasGlyph(emoji)) {
				availableEmoji.add(emoji);
			}
		}

		return availableEmoji.isEmpty() ? new ArrayList<>(TextEmoticons) : availableEmoji;
	}

	public static int getMaxEmojiLevel() {
		return EmojiDataGenerated.GROUPS.length;
	}

	public static boolean isBuiltInEmoji(String emoji) {
		for (String[] group : EmojiDataGenerated.GROUPS) {
			for (String groupedEmoji : group) {
				if (groupedEmoji.equals(emoji)) {
					return true;
				}
			}
		}

		return false;
	}
}
