package pl.shockah.tmlinstaller.os;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.stage.FileChooser;
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
		return new File(System.getProperty("user.home"), "Library/Application Support/Steam/steamapps/common/Terraria/Terraria.app");
	}

	@Nullable
	@Override
	public File browseForTerrariaInstallPath(@Nonnull Window window) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Terraria.app");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Terraria.app", "Terraria.app"));
		chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));
		return chooser.showOpenDialog(window);
	}

	@Nonnull
	@Override
	public File getTerrariaFilesPathRelativeToBasePath(@Nonnull File basePath) {
		return new File(basePath, "Contents/MacOS");
	}
}