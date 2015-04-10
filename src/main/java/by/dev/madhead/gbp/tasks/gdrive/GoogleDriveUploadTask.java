/*
 * Copyright 2015 madhead <siarhei.krukau@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.dev.madhead.gbp.tasks.gdrive;

import by.dev.madhead.gbp.util.Constants;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;

/**
 * Task for uploading things (potentially, your backups) to Google Drive.
 */
public class GoogleDriveUploadTask extends DefaultTask {
	private String clientIdVar = Constants.DEFAULT_GDRIVE_CLIENT_ID_ENV_VAR;
	private String clientId = System.getenv(clientIdVar);
	private String clientSecretVar = Constants.DEFAULT_GDRIVE_CLIENT_SECRET_ENV_VAR;
	private String clientSecret = System.getenv(clientSecretVar);
	private String accessTokenVar = Constants.DEFAULT_GDRIVE_ACCESS_TOKEN_VAR;
	private String accessToken = System.getenv(accessTokenVar);
	private String refreshTokenVar = Constants.DEFAULT_GDRIVE_REFRESH_TOKEN_VAR;
	private String refreshToken = System.getenv(refreshTokenVar);

	private File archive;
	private String mimeType = MediaType.ANY_TYPE.toString();

	/**
	 * Uploads {@link #setArchive(File) specified file} to Google Drive.
	 */
	@TaskAction
	public void run() {
		try {
			final HttpTransport transport = new NetHttpTransport();
			final JsonFactory jsonFactory = new JacksonFactory();
			final GoogleCredential credentials = new GoogleCredential.Builder()
					.setTransport(transport)
					.setJsonFactory(jsonFactory)
					.setClientSecrets(clientId, clientSecret)
					.build()
					.setAccessToken(accessToken)
					.setRefreshToken(refreshToken);
			final Drive drive = new Drive.Builder(transport, jsonFactory, credentials)
					.setApplicationName("backups")
					.build();

			final com.google.api.services.drive.model.File descriptor = new com.google.api.services.drive.model.File();
			final FileContent content = new FileContent(mimeType, archive);

			descriptor.setMimeType(content.getType());
			descriptor.setTitle(content.getFile().getName());

			final Drive.Files.Insert insert = drive.files().insert(descriptor, content);
			final MediaHttpUploader uploader = insert.getMediaHttpUploader();

			uploader.setChunkSize(1 * 1024 * 1024 /* bytes */);
			uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
				@Override
				public void progressChanged(MediaHttpUploader u) throws IOException {
					final double progress = (double) u.getNumBytesUploaded() / content.getLength();

					System.out.printf("\r[%-50.50s] %.2f%%",
							Strings.repeat("#", (int) (progress * 50)), progress * 100);
					System.out.flush();
				}
			});

			insert.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
	}

	/**
	 * Sets name of environment variable which stores Google Drive client ID.
	 *
	 * @param clientIdVar
	 * 		name of environment variable which stores Google Drive client ID.
	 */
	public void setClientIdVar(String clientIdVar) {
		this.clientIdVar = clientIdVar;
		this.clientId = System.getenv(clientIdVar);
	}

	/**
	 * Sets name of environment variable which stores Google Drive client secret.
	 *
	 * @param clientSecretVar
	 * 		name of environment variable which stores Google Drive client secret.
	 */
	public void setClientSecretVar(String clientSecretVar) {
		this.clientSecretVar = clientSecretVar;
		this.clientSecret = System.getenv(clientSecretVar);
	}

	/**
	 * Sets name of environment variable which stores Google Drive access token.
	 *
	 * @param accessTokenVar
	 * 		name of environment variable which stores Google Drive access token.
	 */
	public void setAccessTokenVar(String accessTokenVar) {
		this.accessTokenVar = accessTokenVar;
		this.accessToken = System.getenv(accessTokenVar);
	}

	/**
	 * Sets name of environment variable which stores Google Drive refresh token.
	 *
	 * @param refreshTokenVar
	 * 		name of environment variable which stores Google Drive refresh token.
	 */
	public void setRefreshTokenVar(String refreshTokenVar) {
		this.refreshTokenVar = refreshTokenVar;
		this.refreshToken = System.getenv(refreshTokenVar);
	}

	/**
	 * Sets file for uploading to Google Drive.
	 *
	 * @param archive
	 * 		file for uploading to Google Drive.
	 */
	public void setArchive(File archive) {
		this.archive = archive;
	}

	/**
	 * Sets MIME type of uploaded thing.
	 *
	 * @param mimeType
	 * 		MIME type of uploaded thing.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
