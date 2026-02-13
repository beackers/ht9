package io.github.sspanak.tt9.util.chars;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.sspanak.tt9.preferences.settings.SettingsStatic;

class Emoji extends Punctuation {
	private static final String EMOJI_DOWNLOAD_URL = "https://raw.githubusercontent.com/sspanak/tt9/main/app/src/main/unicode/emoji-test.txt";
	private static final int MENU_GROUPS = 9;

	final private static ArrayList<String> TextEmoticons = new ArrayList<>(Arrays.asList(
		":)", ":D", ":P", ";)", "\\m/", ":-O", ":|", ":("
	));

	final private static Map<String, ArrayList<String>> EmojiGlyphCache = new HashMap<>();
	private static ArrayList<ArrayList<String>> downloadedEmoji;
	private static ArrayList<ArrayList<String>> systemEmoji;

	public static boolean isGraphic(char ch) {
		return !(ch < 256 || Character.isLetterOrDigit(ch) || Character.isAlphabetic(ch));
	}

	@NonNull
	public static ArrayList<String> getEmoji(int level) {
		return getEmoji(level, true, false);
	}

	@NonNull
	public static ArrayList<String> getEmoji(int level, boolean useSimpleLoading) {
		return getEmoji(level, useSimpleLoading, false);
	}

	@NonNull
	public static ArrayList<String> getEmoji(int level, boolean useSimpleLoading, boolean preferDownloaded) {
		ArrayList<ArrayList<String>> groups = getEmojiGroups(preferDownloaded);
		if (level < 0 || level >= groups.size()) {
			return new ArrayList<>();
		}

		String cacheKey = (useSimpleLoading ? "simple:" : "full:") + (preferDownloaded ? "download:" : "system:") + level;
		ArrayList<String> availableEmoji = EmojiGlyphCache.get(cacheKey);
		if (availableEmoji == null) {
			Paint paint = new Paint();
			availableEmoji = new ArrayList<>();
			for (String emoji : groups.get(level)) {
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

	@NonNull
	public static ArrayList<String> getEmojiMenuGroups(boolean preferDownloaded) {
		ArrayList<String> menu = new ArrayList<>();
		ArrayList<ArrayList<String>> groups = getEmojiGroups(preferDownloaded);
		for (int i = 0; i < Math.min(MENU_GROUPS, groups.size()); i++) {
			menu.add(groups.get(i).isEmpty() ? TextEmoticons.get(i % TextEmoticons.size()) : groups.get(i).get(0));
		}
		return menu;
	}

	public static int getMaxEmojiLevel(boolean preferDownloaded) {
		return getEmojiGroups(preferDownloaded).size();
	}

	public static int getMaxEmojiLevel() {
		return getMaxEmojiLevel(false);
	}

	public static boolean isBuiltInEmoji(String emoji) {
		for (ArrayList<String> group : getEmojiGroups(false)) {
			if (group.contains(emoji)) {
				return true;
			}
		}
		return false;
	}

	private static ArrayList<ArrayList<String>> getEmojiGroups(boolean preferDownloaded) {
		if (preferDownloaded) {
			if (downloadedEmoji == null) {
				downloadedEmoji = loadDownloadedEmoji();
			}
			if (!downloadedEmoji.isEmpty()) {
				return downloadedEmoji;
			}
		}

		if (systemEmoji == null) {
			systemEmoji = loadSystemEmoji();
		}
		return systemEmoji;
	}

	private static ArrayList<ArrayList<String>> loadSystemEmoji() {
		ArrayList<ArrayList<String>> groups = new ArrayList<>();
		groups.add(scanRange(0x1F600, 0x1F64F)); // smileys
		groups.add(scanRange(0x1F300, 0x1F5FF)); // symbols
		groups.add(scanRange(0x1F680, 0x1F6FF)); // transport
		groups.add(scanRange(0x1F900, 0x1F9FF)); // supplemental
		groups.add(scanRange(0x2600, 0x26FF));   // misc
		groups.add(scanRange(0x2700, 0x27BF));   // dingbats
		groups.add(scanRange(0x1FA70, 0x1FAFF)); // symbols
		groups.add(scanRange(0x1F1E6, 0x1F1FF)); // flags
		groups.add(new ArrayList<>(TextEmoticons));
		return groups;
	}

	private static ArrayList<String> scanRange(int start, int end) {
		Paint paint = new Paint();
		ArrayList<String> result = new ArrayList<>();
		for (int cp = start; cp <= end && result.size() < 200; cp++) {
			if (!Character.isDefined(cp) || !Character.isValidCodePoint(cp)) {
				continue;
			}
			String symbol = new String(Character.toChars(cp));
			if (paint.hasGlyph(symbol) && symbol.codePointCount(0, symbol.length()) == 1 && !Character.isLetterOrDigit(cp)) {
				result.add(symbol);
			}
		}
		if (result.isEmpty()) {
			result.addAll(TextEmoticons);
		}
		return result;
	}

	private static ArrayList<ArrayList<String>> loadDownloadedEmoji() {
		ArrayList<ArrayList<String>> groups = new ArrayList<>();
		for (int i = 0; i < MENU_GROUPS; i++) {
			groups.add(new ArrayList<>());
		}

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(EMOJI_DOWNLOAD_URL).openConnection();
			connection.setConnectTimeout(SettingsStatic.DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT);
			connection.setReadTimeout(SettingsStatic.DICTIONARY_DOWNLOAD_READ_TIMEOUT);

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				int group = 0;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("# group:")) {
						group = (group + 1) % MENU_GROUPS;
						continue;
					}
					if (line.isEmpty() || line.startsWith("#") || !line.contains("; fully-qualified")) {
						continue;
					}
					String[] parts = line.split("#", 2);
					if (parts.length < 2) {
						continue;
					}
					String[] emojiAndName = parts[1].trim().split(" ", 2);
					if (emojiAndName.length > 0 && groups.get(group).size() < 250) {
						groups.get(group).add(emojiAndName[0]);
					}
				}
			}
		} catch (Exception ignored) {
			return new ArrayList<>();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return groups;
	}

	public static void invalidateEmojiCache() {
		EmojiGlyphCache.clear();
		downloadedEmoji = null;
		systemEmoji = null;
	}
}
