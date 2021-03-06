package pl.shockah.tmlinstaller.version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.annotation.Nonnull;

import pl.shockah.tmlinstaller.TModLoaderInstaller;
import pl.shockah.unicorn.func.Action0;
import pl.shockah.unicorn.func.Action1;

public class BackupVersion implements InstallableVersion {
	@Nonnull
	public final File file;

	public BackupVersion(@Nonnull File file) {
		this.file = file;
	}

	@Nonnull
	@Override
	public String getName() {
		return "Vanilla backup";
	}

	@Override
	public void retrieveAndInstall(@Nonnull File basePath, @Nonnull RetrieveProgressCallback progress, @Nonnull Action0 success, @Nonnull Action1<Throwable> failure) {
		File gameFile = TModLoaderInstaller.os.get().getTerrariaExePathRelativeToBasePath(basePath);

		try {
			progress.onProgress(0f);
			Files.copy(file.toPath(), gameFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			progress.onProgress(1f);
			success.call();
		} catch (IOException e) {
			e.printStackTrace();
			failure.call(e);
		}
	}

	@Override
	public boolean shouldBackup() {
		return false;
	}
}