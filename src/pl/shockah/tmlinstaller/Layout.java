package pl.shockah.tmlinstaller;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;

import javafx.fxml.FXMLLoader;
import pl.shockah.unicorn.UnexpectedException;

public final class Layout<T extends Controller> {
	@Nonnull
	public final String name;

	public Layout(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public T load() {
		try {
			FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource(String.format("layouts/%s.fxml", name))));
			loader.load();
			T controller = loader.getController();
			controller.view = loader.getRoot();
			controller.onLoaded();

			controller.getView().sceneProperty().addListener((observable, oldValue, newValue) -> {
				if (oldValue == null && newValue != null)
					controller.onAddedToScene(newValue);
				else if (oldValue != null && newValue == null)
					controller.onRemovedFromScene(oldValue);
			});
			return controller;
		} catch (IOException e) {
			throw new UnexpectedException(e);
		}
	}
}