package pl.shockah.tmlinstaller;

import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import pl.shockah.tmlinstaller.os.MacOS;
import pl.shockah.tmlinstaller.os.OS;
import pl.shockah.tmlinstaller.os.WindowsOS;
import pl.shockah.unicorn.func.Action0;
import pl.shockah.unicorn.func.Lazy;

public class TModLoaderInstaller extends Application {
	@Nonnull
	public static final Lazy<OS> os = new Lazy<>(() -> {
		String osProperty = System.getProperty("os.name");

		// TODO: Linux support
		if (osProperty.toLowerCase().contains("mac") && osProperty.toLowerCase().contains("os"))
			return new MacOS();
		else
			return new WindowsOS();
	});

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
						//github = GitHub.connectUsingOAuth("2552e547ba340cf508935b056685a824d0c86d56");
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
	public static OkHttpClient getNewClient() {
		return getNewClient(null);
	}

	@Nonnull
	public static OkHttpClient getNewClient(@Nullable OkHttpProgressListener progressListener) {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		executor.allowCoreThreadTimeOut(true);

		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.dispatcher(new Dispatcher(executor))
				.connectTimeout(5, TimeUnit.SECONDS)
				.readTimeout(5, TimeUnit.SECONDS)
				.writeTimeout(5, TimeUnit.SECONDS);

		if (progressListener != null)
			builder = builder.addNetworkInterceptor(chain -> {
				Response originalResponse = chain.proceed(chain.request());
				return originalResponse.newBuilder()
						.body(new OkHttpProgressResponseBody(originalResponse.body(), progressListener))
						.build();
			});

		return builder.build();
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		os.get().initialize();

		primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
		primaryStage.setTitle("tModLoader Installer");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(Layouts.app.load().getRoot()));
		primaryStage.show();
	}

	public interface GithubSuccessCallback {
		void onSuccess(@Nonnull GitHub github) throws IOException;
	}
}