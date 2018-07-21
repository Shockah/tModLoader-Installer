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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.shockah.tmlinstaller.OkHttpProgressResponseBody;
import pl.shockah.tmlinstaller.TModLoaderInstaller;
import pl.shockah.tmlinstaller.os.OS;
import pl.shockah.unicorn.func.Action0;

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
	public void retrieveAndInstall(@Nonnull File terrariaFolder, @Nonnull RetrieveProgressCallback progress, @Nonnull Action0 success, @Nonnull Action0 failure) {
		progress.onProgress(0f);

		try {
			GHAsset asset = getOSSpecificAsset(release);
			if (asset == null) {
				failure.call();
				return;
			}

			Request request = new Request.Builder()
					.url(asset.getBrowserDownloadUrl())
					.build();

			OkHttpClient client = new OkHttpClient.Builder()
					.addNetworkInterceptor(chain -> {
						Response originalResponse = chain.proceed(chain.request());
						return originalResponse.newBuilder()
								.body(new OkHttpProgressResponseBody(originalResponse.body(), (bytesRead, contentLength, done) -> {
									if (!done)
										progress.onProgress(0.5f * bytesRead / contentLength);
								}))
								.build();
					})
					.build();

			client.newCall(request).enqueue(new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {
					failure.call();
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
							File newFile = new File(terrariaFolder, entry.getName());
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
			failure.call();
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