package by.dev.madhead.gbp.tasks.gdrive;

import by.dev.madhead.gbp.model.GoogleDrive;
import com.google.common.base.Preconditions;
import org.gradle.api.DefaultTask;
import org.gradle.model.internal.registry.ModelRegistry;

/**
 * Base class for all Google Drive communications.
 */
abstract class BaseGoogleDriveTask extends DefaultTask {
	protected String clientId;
	protected String clientSecret;

	protected BaseGoogleDriveTask(ModelRegistry modelRegistry) {
		modelRegistry.bindAllReferences();

		final GoogleDrive googleDriveSettings = modelRegistry.realize("backup.googleDrive", GoogleDrive.class);

		this.clientId = googleDriveSettings.getClientId();
		this.clientSecret = googleDriveSettings.getClientSecret();

		Preconditions.checkNotNull(this.clientId, "Google Drive client ID must not be null");
		Preconditions.checkNotNull(this.clientSecret, "Google Drive client secret must not be null");
	}
}
