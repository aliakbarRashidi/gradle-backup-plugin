package by.dev.madhead.gbp

import by.dev.madhead.gbp.tasks.gdrive.ObtainGoogleDriveTokensTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleBackupPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.configure(project) {
			project.task('obtainGoogleDriveTokens', type: ObtainGoogleDriveTokensTask)
		}
	}
}
