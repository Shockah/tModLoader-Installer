package pl.shockah.tmlinstaller.os;

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;

import javafx.stage.FileChooser;
import javafx.stage.Window;

public class MacOS extends OS {
	@Override
	public void initialize() {
		super.initialize();

		URL iconURL = getClass().getClassLoader().getResource("icon.png");
		if (iconURL != null) {
			Image image = new ImageIcon(iconURL).getImage();

			try {
				Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
				Object application = applicationClass.getMethod("getApplication").invoke(null);
				application.getClass().getMethod("setDockIconImage", Image.class).invoke(application, image);
			} catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

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