package pl.shockah.tmlinstaller.os;

import java.io.File;
import java.util.Scanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class WindowsOS extends OS {
	@Nonnull
	@Override
	public String getGithubAssetString() {
		return ".Windows.";
	}

	@Nullable
	@Override
	public File getTerrariaInstallPath() {
		String steamPath = readRegistry("HKCU\\Software\\Valve\\Steam", "SteamPath");
		if (steamPath != null) {
			for (File steamAppsPath : getAllSteamLibraries(new File(steamPath, "steamapps"))) {
				File terrariaPath = new File(steamAppsPath, "common/Terraria");
				if (terrariaPath.exists())
					return terrariaPath;
			}
		}

		return null;
	}

	@Nullable
	@Override
	public File browseForTerrariaInstallPath(@Nonnull Window window) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Terraria.exe");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Terraria.exe", "Terraria.exe"));
		chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));
		File result = chooser.showOpenDialog(window);
		if (result != null)
			result = result.getParentFile();
		return result;
	}

	@Nonnull
	@Override
	public File getTerrariaFilesPathRelativeToBasePath(@Nonnull File basePath) {
		return basePath;
	}

	@Nullable
	private static String readRegistry(@Nonnull String location, @Nonnull String key) {
		try {
			Process process = Runtime.getRuntime().exec(String.format("reg query \"%s\" /v \"%s\"", location, key));
			Scanner scanner = new Scanner(process.getInputStream()).useDelimiter("\\A");
			String output = scanner.hasNext() ? scanner.next() : "";

			for (String line : output.split("\\r?\\n")) {
				if (!line.matches("^\\s+.*$"))
					continue;
				return line.split("\\s+")[3];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}