package by.dev.madhead.gbp.model

import org.gradle.model.Managed

@Managed
interface GoogleDrive {
	String getClientId()

	void setClientId(String clientId)

	String getClientSecret()

	void setClientSecret(String clientSecret)

	String getAccessToken()

	void setAccessToken(String accessToken)

	String getRefreshToken()

	void setRefreshToken(String refreshToken)
}
