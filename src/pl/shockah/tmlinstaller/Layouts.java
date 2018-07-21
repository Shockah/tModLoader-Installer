package pl.shockah.tmlinstaller;

import javax.annotation.Nonnull;

import lombok.experimental.UtilityClass;
import pl.shockah.tmlinstaller.controller.AppController;

@UtilityClass
public final class Layouts {
	@Nonnull
	public static final Layout<AppController> app = new Layout<>("app");
}