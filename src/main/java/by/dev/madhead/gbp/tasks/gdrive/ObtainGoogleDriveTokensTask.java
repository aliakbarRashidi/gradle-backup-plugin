/**
 * Copyright 2015 madhead <siarhei.krukau@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package by.dev.madhead.gbp.tasks.gdrive;

import by.dev.madhead.gbp.util.Constants;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

public class ObtainGoogleDriveTokensTask extends DefaultTask {
	private final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private String clientIdVar = Constants.DEFAULT_GDRIVE_CLIENT_ID_ENV_VAR;
	private String clientId = System.getenv(clientIdVar);
	private String clientSecretVar = Constants.DEFAULT_GDRIVE_CLIENT_SECRET_ENV_VAR;
	private String clientSecret = System.getenv(clientSecretVar);

	@TaskAction
	public void run() {
		final Console console = System.console();

		if (null == console) {
			throw new TaskExecutionException(this,
					new UnsupportedOperationException("This task cannot be run without console."));
		}

		try {
			final HttpTransport transport = new NetHttpTransport();
			final JsonFactory jsonFactory = new JacksonFactory();
			final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
					.Builder(transport, jsonFactory, clientId, clientSecret, Arrays.asList(DriveScopes.DRIVE))
					.build();
			final String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();

			System.out.println("Navigate to the following url: " + Constants.ANSI_HIHGLIGHT_CODE + url +
					Constants.ANSI_RESET_CODE + ", and then paste the authorization code here:");

			final String authorizationCode = console.readLine().trim();
			final GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
					.setRedirectUri(REDIRECT_URI)
					.execute();

			System.out.println("Your access token is " + Constants.ANSI_HIHGLIGHT_CODE + tokenResponse.getAccessToken() +
					Constants.ANSI_RESET_CODE + ". Store it as environment variable (e.g. " +
					Constants.ANSI_HIHGLIGHT_CODE + Constants.DEFAULT_GDRIVE_ACCESS_TOKEN_VAR + Constants.ANSI_RESET_CODE +
					") for future use. " +
					"It will expire in " + tokenResponse.getExpiresInSeconds() + " seconds.");
			System.out.println("Your refresh token is " + Constants.ANSI_HIHGLIGHT_CODE + tokenResponse.getRefreshToken() +
					Constants.ANSI_RESET_CODE + ". Store it as environment variable (e.g. " +
					Constants.ANSI_HIHGLIGHT_CODE + Constants.DEFAULT_GDRIVE_REFRESH_TOKEN_VAR + Constants.ANSI_RESET_CODE +
					") for future use.");
		} catch (IOException ioException) {
			throw new TaskExecutionException(this, ioException);
		}
	}

	public void setClientIdVar(String clientIdVar) {
		this.clientIdVar = clientIdVar;
		this.clientId = System.getenv(clientIdVar);
	}

	public void setClientSecretVar(String clientSecretVar) {
		this.clientSecretVar = clientSecretVar;
		this.clientSecret = System.getenv(clientSecretVar);
	}
}
