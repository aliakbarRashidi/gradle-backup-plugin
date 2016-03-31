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

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.base.Preconditions;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.model.internal.registry.ModelRegistry;

import javax.inject.Inject;

/**
 * Lists all the files in a target Google Drive directory.
 */
public class GoogleDriveListTask extends GoogleDriveTask {
	private static final int FILENAME_COLUMN_WIDTH = 50;

	private String[] path;

	@Inject
	public GoogleDriveListTask(ModelRegistry modelRegistry) {
		super(modelRegistry);
	}

	/**
	 * Lists all the files in a target Google Drive directory.
	 */
	@TaskAction
	public void run() {
		try {
			Preconditions.checkNotNull(path, "Target directory must be specified");

			final File targetDirectory = locateTargetDirectory(path);
			final FileList files = drive.files().list().setQ("('" + targetDirectory.getId() + "' in parents)").execute();

			if (null != files) {
				for (File file : files.getItems()) {
					System.out.println(
							String.format("%-" + FILENAME_COLUMN_WIDTH + "s [0x%S]",
									file.getOriginalFilename().substring(0, Math.min(file.getOriginalFilename().length(),
											FILENAME_COLUMN_WIDTH)),
									file.getMd5Checksum()));
				}
			}
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
	}

	/**
	 * Sets destination path inside Google Drive starting from the root, like ["backups", "projects",
	 * "myBackupedProject"].
	 *
	 * @param path
	 * 		destination path inside the Drive or null if you want to place files in the root.
	 */
	public void setPath(String[] path) {
		this.path = path;
	}
}
