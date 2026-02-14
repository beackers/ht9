package io.github.sspanak.tt9.preferences.screens.modePredictive;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class DropDownEmojiSource extends EnhancedDropDownPreference {
	public static final String NAME = "pref_emoji_source";

	public DropDownEmojiSource(@NonNull Context context) { super(context); }
	public DropDownEmojiSource(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); }
	public DropDownEmojiSource(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	public DropDownEmojiSource(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	protected int getDisplayTitle() {
		return R.string.pref_emoji_source;
	}

	@Override
	public DropDownEmojiSource populate(@NonNull SettingsStore settings) {
		add(SettingsStore.EMOJI_SOURCE_SYSTEM, getContext().getString(R.string.pref_emoji_source_system));
		add(SettingsStore.EMOJI_SOURCE_DOWNLOAD, getContext().getString(R.string.pref_emoji_source_download));
		commitOptions();
		setValue(settings.getEmojiSource());
		setDefaultValue(SettingsStore.EMOJI_SOURCE_SYSTEM);
		return this;
	}
}
