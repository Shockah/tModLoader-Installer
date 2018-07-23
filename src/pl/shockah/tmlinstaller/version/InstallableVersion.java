package pl.shockah.tmlinstaller.version;

import java.io.File;

import javax.annotation.Nonnull;

import pl.shockah.unicorn.func.Action0;
import pl.shockah.unicorn.func.Action1;

public interface InstallableVersion {
	@Nonnull
	String getName();

	void retrieveAndInstall(@Nonnull File basePath, @Nonnull RetrieveProgressCallback progress, @Nonnull Action0 success, @Nonnull Action1<Throwable> failure);

	boolean shouldBackup();

	interface RetrieveProgressCallback {
		void onProgress(float f);
	}
}