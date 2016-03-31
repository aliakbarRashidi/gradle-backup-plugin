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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.common.base.Preconditions;
import org.fusesource.jansi.Ansi;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.model.internal.registry.ModelRegistry;

import javax.inject.Inject;
import java.io.Console;
import java.io.IOException;
import java.util.Arrays;

/**
 * Helper task for obtaining access and refresh tokens for Google Drive.
 */
public class ObtainGoogleDriveTokensTask extends BaseGoogleDriveTask {
	private final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	@Inject
	public ObtainGoogleDriveTokensTask(ModelRegistry modelRegistry) {
		super(modelRegistry);
	}

	/**
	 * Initiates Google Drive tokens obtaining flow.
	 */
	@TaskAction
	public void run() {
		final Console console = System.console();

		if (null == console) {
			throw new TaskExecutionException(this,
					new UnsupportedOperationException("This task cannot be run without console."));
		}

		try {
			Preconditions.checkNotNull(this.clientId, "Google Drive client ID must not be null");
			Preconditions.checkNotNull(this.clientSecret, "Google Drive client secret must not be null");

			final HttpTransport transport = new NetHttpTransport();
			final JsonFactory jsonFactory = new JacksonFactory();
			final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
					.Builder(transport, jsonFactory, clientId, clientSecret, Arrays.asList(DriveScopes.DRIVE))
					.build();
			final String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();

			System.out.println((new Ansi()).
					a("Navigate to the following url: ").
					fg(Ansi.Color.YELLOW).
					a(url).
					reset().
					a(", and then paste the authorization code here:"));

			final String authorizationCode = console.readLine().trim();
			final GoogleTokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
					.setRedirectUri(REDIRECT_URI)
					.execute();

			System.out.println((new Ansi()).
					a("Your access token is ").
					fg(Ansi.Color.YELLOW).
					a(tokenResponse.getAccessToken()).
					reset().
					a(". Store it somewhere for future use. It will expire in ").
					a(tokenResponse.getExpiresInSeconds()).
					a(" seconds"));
			System.out.println((new Ansi()).
					a("Your refresh token is ").
					fg(Ansi.Color.YELLOW).
					a(tokenResponse.getRefreshToken()).
					reset().
					a(". Store it somewhere for future use."));
		} catch (IOException ioException) {
			throw new TaskExecutionException(this, ioException);
		}
	}
}
