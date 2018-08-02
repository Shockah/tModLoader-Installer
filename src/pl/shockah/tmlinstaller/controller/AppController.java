package pl.shockah.tmlinstaller.controller;

import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import pl.shockah.tmlinstaller.TModLoaderInstaller;
import pl.shockah.tmlinstaller.version.BackupVersion;
import pl.shockah.tmlinstaller.version.GHReleaseInstallableVersion;
import pl.shockah.tmlinstaller.version.InstallableVersion;
import pl.shockah.unicorn.javafx.Controller;

public class AppController extends Controller {
	@FXML
	private TitledPane installPane;

	@FXML
	private TitledPane charactersPane;

	@FXML
	private TitledPane worldsPane;

	@FXML
	private TextField pathTextField;

	@FXML
	private Button browseButton;

	@FXML
	private ComboBox<InstallableVersion> versionComboBox;

	@FXML
	private Button installButton;

	@FXML
	private ListView<String> vanillaCharactersList;

	@FXML
	private ListView<String> moddedCharactersList;

	@FXML
	private ListView<String> vanillaWorldsList;

	@FXML
	private ListView<String> moddedWorldsList;

	@FXML
	private Button copyCharacterButton;

	@FXML
	private Button copyWorldButton;

	@FXML
	private ProgressBar progressBar;

	@Nonnull
	private final ObservableList<GHReleaseInstallableVersion> githubInstallableVersions = FXCollections.observableArrayList();

	private ObservableValue<File> terrariaPath;

	@Nonnull
	private BooleanProperty installing = new SimpleBooleanProperty(this, "installing", false);

	@Override
	protected void onLoaded() {
		super.onLoaded();

		createObservables();
		setupViews();
		setupBindings();

		((Pane)charactersPane.getParent()).getChildren().remove(charactersPane);
		((Pane)worldsPane.getParent()).getChildren().remove(worldsPane);
	}

	private void createObservables() {
		terrariaPath = Bindings.createObjectBinding(() -> {
			File file = new File(pathTextField.getText());
			if (!file.exists())
				file = null;

			if (file != null) {
				File gameFile = TModLoaderInstaller.os.get().getTerrariaExePathRelativeToBasePath(file);
				if (!gameFile.exists())
					file = null;
			}

			return file;
		}, pathTextField.textProperty());
	}

	private void setupViews() {
		versionComboBox.setCellFactory(new Callback<ListView<InstallableVersion>, ListCell<InstallableVersion>>() {
			@Override
			public ListCell<InstallableVersion> call(ListView<InstallableVersion> param) {
				return new ListCell<InstallableVersion>() {
					@Override
					protected void updateItem(InstallableVersion item, boolean empty) {
						super.updateItem(item, empty);

						if (empty || item == null)
							setText("");
						else
							setText(item.getName());
					}
				};
			}
		});
		versionComboBox.setButtonCell(versionComboBox.getCellFactory().call(null));

		File terrariaPath = TModLoaderInstaller.os.get().getTerrariaInstallPath();
		if (terrariaPath != null && terrariaPath.exists())
			pathTextField.setText(terrariaPath.getAbsolutePath());
	}

	private void setupBindings() {
		githubInstallableVersions.addListener((ListChangeListener<GHReleaseInstallableVersion>)c -> {
			updateVersions();
		});

		installPane.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			return installing.get() || versionComboBox.getItems().isEmpty();
		}, installing, new SimpleListProperty<>(versionComboBox.getItems()).emptyProperty()));

		installButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
			if (versionComboBox.getSelectionModel().getSelectedItem() == null)
				return true;
			if (terrariaPath.getValue() == null)
				return true;
			return false;
		}, versionComboBox.getSelectionModel().selectedItemProperty(), terrariaPath));
	}

	@Override
	protected void onAddedToScene(@Nonnull Scene scene) {
		super.onAddedToScene(scene);
		retrieveAndUpdateVersions();
	}

	public void retrieveAndUpdateVersions() {
		versionComboBox.getItems().clear();

		charactersPane.setDisable(true);
		worldsPane.setDisable(true);

		TModLoaderInstaller.getGithub(github -> {
			GHRepository repository = github.getRepository("blushiemagic/tModLoader");

			List<GHReleaseInstallableVersion> newVersions = new ArrayList<>();
			for (GHRelease release : repository.listReleases().asList()) {
				if (release.getName().isEmpty())
					continue;
				newVersions.add(new GHReleaseInstallableVersion(release));
			}

			Platform.runLater(() -> {
				githubInstallableVersions.setAll(newVersions);
			});
		});
	}

	public void updateVersions() {
		List<InstallableVersion> newVersions = new ArrayList<>();
		newVersions.addAll(githubInstallableVersions);

		// TODO: backup needs to be OS-specific, I guess

		File file = terrariaPath.getValue();
		if (file != null) {
			File backupFile = new File(file, "Terraria.bak.exe");
			if (backupFile.exists())
				newVersions.add(new BackupVersion(backupFile));
		}

		versionComboBox.getItems().setAll(newVersions);

		if (!newVersions.isEmpty())
			versionComboBox.getSelectionModel().select(0);
	}

	@FXML
	private void onBrowseAction(ActionEvent event) {
		File file = TModLoaderInstaller.os.get().browseForTerrariaInstallPath(getRoot().getScene().getWindow());
		if (file == null || !file.exists())
			return;
		pathTextField.setText(file.getAbsolutePath());
	}

	@FXML
	private void onInstallAction(ActionEvent event) {
		installing.set(true);

		Thread thread = new Thread(() -> {
			File basePath = new File(pathTextField.getText());
			File gameFile = TModLoaderInstaller.os.get().getTerrariaExePathRelativeToBasePath(basePath);
			File backupFile = TModLoaderInstaller.os.get().getTerrariaExeBackupPathRelativeToBasePath(basePath);

			try {
				InstallableVersion version = versionComboBox.getSelectionModel().getSelectedItem();
				progressBar.setProgress(0f);
				if (version.shouldBackup() && gameFile.exists() && !backupFile.exists())
					Files.copy(gameFile.toPath(), backupFile.toPath());

				version.retrieveAndInstall(basePath, f -> {
					progressBar.setProgress(f);
				}, () -> Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Success");
					alert.setHeaderText(null);
					alert.setContentText(String.format("Successfully installed %s.", version.getName()));
					alert.show();

					installing.set(false);
				}), throwable -> {
					if (throwable != null)
						showThrowableError(throwable);

					installing.set(false);
				});
			} catch (IOException e) {
				e.printStackTrace();
				showThrowableError(e);

				installing.set(false);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void showThrowableError(@Nonnull Throwable throwable) {
		if (Platform.isFxApplicationThread()) {
			showThrowableErrorNow(throwable);
		} else {
			Platform.runLater(() -> showThrowableErrorNow(throwable));
		}
	}

	private void showThrowableErrorNow(@Nonnull Throwable throwable) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(null);
		alert.setContentText(String.format("There was an error while installing:\n%s", throwable.getMessage()));
		alert.show();
	}
}