package io.github.sspanak.tt9.languages;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.ime.modes.helpers.Sequences;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class EmojiLanguage extends Language {
	private static final int PAGE_SIZE = 6;

	public static class EmojiBrowseState {
		public final int category;
		public final int page;

		public EmojiBrowseState(int category, int page) {
			this.category = category;
			this.page = page;
		}
	}

	private final Sequences seq;
	private final boolean useSimpleLoading;
	private final boolean preferDownloadedEmoji;

	public EmojiLanguage(Sequences sequences) {
		this(sequences, true, false);
	}

	public EmojiLanguage(Sequences sequences, boolean useSimpleLoading, boolean preferDownloadedEmoji) {
		id = Integer.parseInt(new Sequences().EMOJI_SEQUENCE); // always use the unprefixed sequence for ID
		locale = Locale.ROOT;
		abcString = "emoji";
		code = "emj";
		currency = "";
		name = "Emoji";
		seq = sequences == null ? new Sequences() : sequences;
		this.useSimpleLoading = useSimpleLoading;
		this.preferDownloadedEmoji = preferDownloadedEmoji;
	}

	@NonNull
	@Override
	public String getDigitSequenceForWord(String word) {
		return isValidWord(word) && Characters.isBuiltInEmoji(word) ? seq.EMOJI_SEQUENCE : "";
	}

	@NonNull
	public ArrayList<String> getKeyCharacters(int key, @NonNull EmojiBrowseState state) {
		if (key != 1 || state.category < 0) {
			return new ArrayList<>();
		}

		ArrayList<String> categoryItems = Characters.getEmoji(state.category, useSimpleLoading, preferDownloadedEmoji);
		if (categoryItems.isEmpty()) {
			return categoryItems;
		}

		int start = state.page * PAGE_SIZE;
		if (start >= categoryItems.size()) {
			start = 0;
		}

		int end = Math.min(categoryItems.size(), start + PAGE_SIZE);
		return new ArrayList<>(categoryItems.subList(start, end));
	}

	/**
	 * Legacy helper kept for brief-list mode compatibility.
	 */
	@NonNull
	public ArrayList<String> getKeyCharacters(int key, int characterGroup) {
		return key == 1 && characterGroup >= 0 ? Characters.getEmoji(characterGroup, useSimpleLoading, preferDownloadedEmoji) : new ArrayList<>();
	}

	@NonNull
	public ArrayList<String> getMenuGroups() {
		return Characters.getEmojiMenuGroups(preferDownloadedEmoji);
	}

	@NonNull
	@Override
	public ArrayList<String> getKeyCharacters(int key) {
		return getKeyCharacters(key, new EmojiBrowseState(0, 0));
	}

	@Override
	public boolean isValidWord(String word) {
		return TextTools.isGraphic(word);
	}

	public static String validateEmojiSequence(@NonNull Sequences seq, @NonNull String sequence, int next) {
		if (!sequence.startsWith(seq.EMOJI_SEQUENCE)) {
			return sequence + next;
		}

		if (sequence.length() == seq.EMOJI_SEQUENCE.length()) {
			if (next < 1 || next > 9) {
				return sequence;
			}
			return toEmojiSequence(seq, next - 1, 0);
		}

		if (next == Sequences.CHARS_1_KEY) {
			EmojiBrowseState state = getBrowseState(seq, sequence);
			return toEmojiSequence(seq, state.category, state.page + 1);
		}

		return sequence;
	}

	@NonNull
	public static String nextEmojiCategory(@NonNull Sequences seq, @NonNull String sequence) {
		if (!sequence.startsWith(seq.EMOJI_SEQUENCE)) {
			return sequence;
		}

		EmojiBrowseState state = getBrowseState(seq, sequence);
		return toEmojiSequence(seq, state.category + 1, 0);
	}

	@NonNull
	public static EmojiBrowseState getBrowseState(@NonNull Sequences seq, @NonNull String sequence) {
		final int categoryCount = Math.max(1, Characters.getMaxEmojiLevel(false));

		if (sequence.length() >= seq.EMOJI_SEQUENCE.length() + 2) {
			int category = sequence.charAt(seq.EMOJI_SEQUENCE.length()) - '0';
			int page = sequence.charAt(seq.EMOJI_SEQUENCE.length() + 1) - '0';
			category = Math.floorMod(category, categoryCount);
			page = Math.floorMod(page, getPageCount(category));
			return new EmojiBrowseState(category, page);
		}

		int oldLevel = Math.max(0, sequence.length() - seq.EMOJI_SEQUENCE.length());
		return new EmojiBrowseState(Math.floorMod(oldLevel, categoryCount), 0);
	}

	@NonNull
	private static String toEmojiSequence(@NonNull Sequences seq, int category, int page) {
		int categoryCount = Math.max(1, Characters.getMaxEmojiLevel(false));
		int normalizedCategory = Math.floorMod(category, categoryCount);
		int normalizedPage = Math.floorMod(page, getPageCount(normalizedCategory));

		return seq.EMOJI_SEQUENCE + normalizedCategory + normalizedPage;
	}

	private static int getPageCount(int category) {
		int size = Characters.getEmoji(category).size();
		return Math.max(1, (int) Math.ceil(size / (float) PAGE_SIZE));
	}
}
