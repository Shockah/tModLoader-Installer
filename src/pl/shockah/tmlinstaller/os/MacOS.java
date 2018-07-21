package pl.shockah.tmlinstaller.os;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.stage.Window;

public class MacOS extends OS {
	@Nonnull
	@Override
	public String getGithubAssetString() {
		return ".Mac.";
	}

	@Nullable
	@Override
	public File getTerrariaInstallPath() {
		return new File("~/Library/Application Support/Steam/steamapps/common/Terraria");
	}

	@Nullable
	@Override
	public File browseForTerrariaInstallPath(@Nonnull Window window) {
		return null;
	}
}