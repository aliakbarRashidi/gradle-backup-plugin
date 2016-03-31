package by.dev.madhead.gbp.tasks.gdrive;

import by.dev.madhead.gbp.model.GoogleDrive;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;
import org.gradle.model.internal.registry.ModelRegistry;

import java.io.IOException;

/**
 * Base class for all Google Drive API related operations.
 */
abstract class GoogleDriveTask extends BaseGoogleDriveTask {
	protected String accessToken;
	protected String refreshToken;
	protected Drive drive;

	protected GoogleDriveTask(ModelRegistry modelRegistry) {
		super(modelRegistry);

		modelRegistry.bindAllReferences();

		final GoogleDrive googleDriveSettings = modelRegistry.realize("backup.googleDrive", GoogleDrive.class);

		this.accessToken = googleDriveSettings.getAccessToken();
		this.refreshToken = googleDriveSettings.getRefreshToken();

		Preconditions.checkNotNull(this.accessToken, "Google Drive access token must not be null");
		Preconditions.checkNotNull(this.refreshToken, "Google Drive refresh token must not be null");

		final HttpTransport transport = new NetHttpTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		final GoogleCredential credentials = new GoogleCredential.Builder()
				.setTransport(transport)
				.setJsonFactory(jsonFactory)
				.setClientSecrets(clientId, clientSecret)
				.build()
				.setAccessToken(accessToken)
				.setRefreshToken(refreshToken);
		this.drive = new Drive.Builder(transport, jsonFactory, credentials)
				.setApplicationName("backups")
				.build();
	}

	protected File locateTargetDirectory(final String[] path) throws IOException {
		File result = null;

		if ((path != null) && (path.length > 0)) {
			for (int i = 0; i < path.length; i++) {
				final StringBuilder query = new StringBuilder();

				query.append("(title='");
				query.append(path[i]);
				query.append("')");

				if (null != result) {
					query.append(" and ");
					query.append("('");
					query.append(result.getId());
					query.append("' in parents)");
				}

				final FileList files = drive.files().list().setQ(query.toString()).execute();

				if ((null == files) || (null == files.getItems()) || (files.getItems().isEmpty())) {
					throw new IllegalArgumentException("Invalid Google Drive path. Forgot to create folders?");
				}

				result = files.getItems().get(0);
			}
		}

		if ((null != result) && (!"application/vnd.google-apps.folder".equals(result.getMimeType()))) {
			throw new IllegalArgumentException("Invalid Google Drive path. Destination exists, but it's not a folder.");
		}

		return result;
	}
}
