package pl.shockah.tmlinstaller;

import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pl.shockah.tmlinstaller.os.OS;
import pl.shockah.tmlinstaller.os.WindowsOS;
import pl.shockah.unicorn.func.Action0;

public class TModLoaderInstaller extends Application {
	private static OS os;

	@Nonnull
	private static final Object osLock = new Object();

	private static GitHub github;

	@Nonnull
	private static final Object githubLock = new Object();

	public static void getGithub(@Nonnull GithubSuccessCallback success) {
		getGithub(success, () -> {
			System.out.println("failed");
		});
	}

	public static void getGithub(@Nonnull GithubSuccessCallback success, @Nonnull Action0 failure) {
		Thread thread = new Thread(() -> {
			synchronized (githubLock) {
				try {
					if (github == null) {
						Logger.getLogger(GitHub.class.getName()).setLevel(Level.ALL);
						//github = GitHub.connectUsingOAuth("xxx");
						github = GitHub.connectAnonymously();
					}
					success.onSuccess(github);
				} catch (IOException e) {
					e.printStackTrace();
					failure.call();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Nonnull
	public static OS getOS() {
		synchronized (osLock) {
			if (os == null) {
				// TODO: detect OS
				os = new WindowsOS();
			}
			return os;
		}
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		primaryStage.setTitle("tModLoader Installer");
		primaryStage.setScene(new Scene(Layouts.app.load().getView()));
		primaryStage.show();
	}

	public interface GithubSuccessCallback {
		void onSuccess(@Nonnull GitHub github) throws IOException;
	}
}