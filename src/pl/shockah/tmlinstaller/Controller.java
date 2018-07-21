package pl.shockah.tmlinstaller;

import javax.annotation.Nonnull;

import javafx.scene.Scene;
import javafx.scene.layout.Region;
import lombok.Getter;

public abstract class Controller {
	@Getter
	Region view;

	protected void onLoaded() {
	}

	protected void onAddedToScene(@Nonnull Scene scene) {
	}

	protected void onRemovedFromScene(@Nonnull Scene scene) {
	}
}