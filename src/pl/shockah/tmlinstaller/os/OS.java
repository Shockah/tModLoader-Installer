package pl.shockah.tmlinstaller.os;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.stage.Window;

public abstract class OS {
	@Nonnull
	private static final Pattern libraryFolderPattern = Pattern.compile("\\s*\"\\d+\"\\s+\"(.*)\"\\s*");

	@Nonnull
	public abstract String getGithubAssetString();

	@Nullable
	public abstract File getTerrariaInstallPath();

	@Nullable
	public abstract File browseForTerrariaInstallPath(@Nonnull Window window);

	@Nonnull
	protected List<File> getAllSteamLibraries(@Nonnull File baseSteamAppsPath) {
		List<File> result = new ArrayList<>();
		result.add(baseSteamAppsPath);

		File libraryFoldersFile = new File(baseSteamAppsPath, "libraryfolders.vdf");
		if (!libraryFoldersFile.exists())
			return result;

		try {
			for (String line : Files.readAllLines(libraryFoldersFile.toPath())) {
				Matcher matcher = libraryFolderPattern.matcher(line);
				if (matcher.find()) {
					String potentialPathString = matcher.group(1);
					File potentialPath = new File(potentialPathString);
					if (potentialPath.exists())
						result.add(new File(potentialPath, "steamapps"));
				}
			}
		} catch (IOException e) {
		}
		return result;
	}
}