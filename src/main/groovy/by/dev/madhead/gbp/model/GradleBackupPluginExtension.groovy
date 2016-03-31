package by.dev.madhead.gbp.model

import org.gradle.model.Managed

@Managed
interface GradleBackupPluginExtension {
	GoogleDrive getGoogleDrive()
}
