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

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Task for uploading things (potentially, your backups) to Google Drive.
 */
public class GoogleDriveUploadTask extends BaseGoogleDriveTask {
	private File archive;
	private String mimeType = MediaType.ANY_TYPE.toString();
	private String[] path;
	private boolean listenForUpload = false;

	/**
	 * Uploads {@link #setArchive(File) specified file} to Google Drive.
	 */
	@TaskAction
	public void run() {
		super.run();

		try {
			Preconditions.checkNotNull(this.archive, "Archive must not be null");
			Preconditions.checkArgument(this.archive.exists(), "Archive must exist");
			Preconditions.checkArgument(this.archive.isFile(), "Archive must be a file");

			final Drive drive = constructDrive();

			final com.google.api.services.drive.model.File parent = locateParent(drive);

			final com.google.api.services.drive.model.File descriptor = new com.google.api.services.drive.model.File();
			final FileContent content = new FileContent(mimeType, archive);

			if (null != parent) {
				descriptor.setParents(Arrays.<ParentReference>asList(new ParentReference().setId(parent.getId())));
			}
			descriptor.setMimeType(content.getType());
			descriptor.setTitle(content.getFile().getName());

			final Drive.Files.Insert insert = drive.files().insert(descriptor, content);
			final MediaHttpUploader uploader = insert.getMediaHttpUploader();

			uploader.setChunkSize(1 * 1024 * 1024 /* bytes */);

			if (listenForUpload) {
				uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
					@Override
					public void progressChanged(MediaHttpUploader u) throws IOException {
						final double progress = (double) u.getNumBytesUploaded() / content.getLength();

						System.out.printf("\r[%-50.50s] %.2f%%", Strings.repeat("#", (int) (progress * 50)), progress * 100);
						System.out.flush();
					}
				});
			}

			insert.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
	}

	private com.google.api.services.drive.model.File locateParent(final Drive drive) throws IOException {
		com.google.api.services.drive.model.File result = null;

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
			throw new IllegalArgumentException("Invalid Google Drive path. Destination exists, but it's not a folder" +
					".");
		}

		return result;
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
		Preconditions.checkNotNull(this.mimeType, "MIME type must not be null");
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

	/**
	 * Sets whether to listen for upload process and print progressbar or not.
	 *
	 * @param listenForUpload
	 * 		whether to listen for upload process and print progressbar or not.
	 */
	public void setListenForUpload(boolean listenForUpload) {
		this.listenForUpload = listenForUpload;
	}
}
