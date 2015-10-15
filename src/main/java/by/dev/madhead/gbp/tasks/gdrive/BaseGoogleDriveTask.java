package by.dev.madhead.gbp.tasks.gdrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.base.Preconditions;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskExecutionException;

public class BaseGoogleDriveTask extends DefaultTask {
	protected String clientId;
	protected String clientSecret;
	protected String accessToken;
	protected String refreshToken;

	protected void run() {
		try {
			Preconditions.checkNotNull(this.clientId, "Google Drive client ID must not be null");
			Preconditions.checkNotNull(this.clientSecret, "Google Drive client secret must not be null");
			Preconditions.checkNotNull(this.accessToken, "Google Drive access token must not be null");
			Preconditions.checkNotNull(this.refreshToken, "Google Drive refresh token must not be null");
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
	}

	protected Drive constructDrive() {
		final HttpTransport transport = new NetHttpTransport();
		final JsonFactory jsonFactory = new JacksonFactory();
		final GoogleCredential credentials = new GoogleCredential.Builder()
				.setTransport(transport)
				.setJsonFactory(jsonFactory)
				.setClientSecrets(clientId, clientSecret)
				.build()
				.setAccessToken(accessToken)
				.setRefreshToken(refreshToken);
		return new Drive.Builder(transport, jsonFactory, credentials)
				.setApplicationName("backups")
				.build();
	}

	/**
	 * Sets Google Drive client ID.
	 *
	 * @param clientId
	 * 		Google Drive client ID.
	 */
	public void setClientId(String clientId) {
		Preconditions.checkNotNull(clientId, "Google Drive client ID must not be null");
		this.clientId = clientId;
	}

	/**
	 * Sets Google Drive client secret.
	 *
	 * @param clientSecret
	 * 		Google Drive client secret.
	 */
	public void setClientSecret(String clientSecret) {
		Preconditions.checkNotNull(clientSecret, "Google Drive client secret must not be null");
		this.clientSecret = clientSecret;
	}

	/**
	 * Sets Google Drive access token.
	 *
	 * @param accessToken
	 * 		Google Drive access token.
	 */
	public void setAccessToken(String accessToken) {
		Preconditions.checkNotNull(accessToken, "Google Drive access token must not be null");
		this.accessToken = accessToken;
	}

	/**
	 * Sets Google Drive refresh token.
	 *
	 * @param refreshToken
	 * 		Google Drive refresh token.
	 */
	public void setRefreshToken(String refreshToken) {
		Preconditions.checkNotNull(refreshToken, "Google Drive refresh token must not be null");
		this.refreshToken = refreshToken;
	}
}
