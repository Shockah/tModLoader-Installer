package pl.shockah.tmlinstaller.version;

import java.io.File;

import javax.annotation.Nonnull;

import pl.shockah.unicorn.func.Action0;

public interface InstallableVersion {
	@Nonnull
	String getName();

	void retrieveAndInstall(@Nonnull File terrariaFolder, @Nonnull RetrieveProgressCallback progress, @Nonnull Action0 success, @Nonnull Action0 failure);

	boolean shouldBackup();

	interface RetrieveProgressCallback {
		void onProgress(float f);
	}
}