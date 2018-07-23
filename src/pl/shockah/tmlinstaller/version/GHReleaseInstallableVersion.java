package pl.shockah.tmlinstaller.version;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.shockah.tmlinstaller.OkHttpProgressResponseBody;
import pl.shockah.tmlinstaller.TModLoaderInstaller;
import pl.shockah.tmlinstaller.os.OS;
import pl.shockah.unicorn.func.Action0;
import pl.shockah.unicorn.func.Action1;

public class GHReleaseInstallableVersion implements InstallableVersion {
	@Nonnull
	private static final DateFormat dateFormat = SimpleDateFormat.getDateInstance();

	@Nonnull
	public final GHRelease release;

	public GHReleaseInstallableVersion(@Nonnull GHRelease release) {
		this.release = release;
	}

	@Nonnull
	@Override
	public String getName() {
		return String.format("tModLoader %s (%s)", release.getTagName(), dateFormat.format(release.getPublished_at()));
	}

	@Override
	public void retrieveAndInstall(@Nonnull File basePath, @Nonnull RetrieveProgressCallback progress, @Nonnull Action0 success, @Nonnull Action1<Throwable> failure) {
		progress.onProgress(0f);

		try {
			GHAsset asset = getOSSpecificAsset(release);
			if (asset == null) {
				// TODO: handle potential old releases (Windows-only)
				failure.call(null);
				return;
			}

			TModLoaderInstaller.getNewClient((bytesRead, contentLength, done) -> {
				if (!done)
					progress.onProgress(0.5f * bytesRead / contentLength);
			}).newCall(new Request.Builder()
					.url(asset.getBrowserDownloadUrl())
					.build()
			).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					failure.call(e);
				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					byte[] responseBytes = response.body().bytes();
					progress.onProgress(0.5f);

					int totalContentSize = 0;
					try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(responseBytes))) {
						ZipEntry entry;
						while ((entry = zip.getNextEntry()) != null) {
							totalContentSize += entry.getSize();
						}
					}

					int currentContentSize = 0;
					try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(responseBytes))) {
						DataInputStream data = new DataInputStream(zip);
						ZipEntry entry;
						while ((entry = zip.getNextEntry()) != null) {
							byte[] entryBytes = new byte[(int)entry.getSize()];
							data.readFully(entryBytes);
							File newFile = new File(TModLoaderInstaller.getOS().getTerrariaFilesPathRelativeToBasePath(basePath), entry.getName());
							newFile.getParentFile().mkdirs();
							Files.write(newFile.toPath(), entryBytes);
							currentContentSize += entryBytes.length;
							progress.onProgress(0.5f + 0.5f * currentContentSize / totalContentSize);
						}
					}

					success.call();
				}
			});
		} catch (IOException e) {
			failure.call(e);
		}
	}

	@Nullable
	private GHAsset getOSSpecificAsset(@Nonnull GHRelease release) throws IOException {
		// TODO: detect OS
		return getOSSpecificAsset(release, TModLoaderInstaller.getOS());
	}

	@Nullable
	private GHAsset getOSSpecificAsset(@Nonnull GHRelease release, @Nonnull OS os) throws IOException {
		return release.getAssets().stream()
				.filter(asset -> asset.getName().contains(os.getGithubAssetString()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public boolean shouldBackup() {
		return true;
	}
}