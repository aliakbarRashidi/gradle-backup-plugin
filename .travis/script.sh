#!/bin/sh

# Only upload artifacts from upstream's master build on Oracle JDK 7
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ] && [ "${JAVA_HOME}" = "/usr/lib/jvm/java-7-oracle" ]; then
	if grep -q "SNAPSHOT" gradle.properties; then
		echo "Uploading SNAPSHOT to https://oss.jfrog.org"
		./gradlew generatePomFileForMainPublication artifactoryPublish
	else
		echo "Uploading RELEASE to https://bintray.com/"
		./gradlew bintrayUpload
	fi
fi
