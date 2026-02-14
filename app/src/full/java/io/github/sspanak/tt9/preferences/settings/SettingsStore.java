package io.github.sspanak.tt9.preferences.settings;

import android.content.Context;

import androidx.annotation.NonNull;

public class SettingsStore extends SettingsStatic {
	public static final String EMOJI_SOURCE_SYSTEM = "SYSTEM";
	public static final String EMOJI_SOURCE_DOWNLOAD = "DOWNLOAD";

	public SettingsStore(@NonNull Context context) { super(context); }
}
