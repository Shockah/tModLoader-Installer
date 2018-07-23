package pl.shockah.tmlinstaller.os;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.stage.Window;

public class LinuxOS extends OS {
	@Nonnull
	@Override
	public String getGithubAssetString() {
		return ".Linux.";
	}

	@Nullable
	@Override
	public File getTerrariaInstallPath() {
		return null;
	}

	@Nullable
	@Override
	public File browseForTerrariaInstallPath(@Nonnull Window window) {
		return null;
	}

	@Nonnull
	@Override
	public File getTerrariaFilesPathRelativeToBasePath(@Nonnull File basePath) {
		return null;
	}
}