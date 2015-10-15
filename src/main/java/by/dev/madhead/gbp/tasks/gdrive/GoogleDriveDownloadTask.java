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

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.services.drive.Drive;
import com.google.common.base.Strings;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.IOException;

/**
 * Task for downloading things (potentially, your backups) from Google Drive.
 */
public class GoogleDriveDownloadTask extends BaseGoogleDriveTask {
	private String fileId;
	private boolean listenForDownload = false;

	/**
	 * Downloads {@link #setFileId(String) specified file} from Google Drive.
	 */
	@TaskAction
	@Override
	public void run() {
		super.run();

		try {
			final Drive drive = constructDrive();
			final Drive.Files.Get get = drive.files().get(fileId);
			final MediaHttpDownloader downloader = get.getMediaHttpDownloader();

			downloader.setChunkSize(1 * 1024 * 1024 /* bytes */);

			if (listenForDownload) {
				downloader.setProgressListener(new MediaHttpDownloaderProgressListener() {
					@Override
					public void progressChanged(MediaHttpDownloader d) throws IOException {
						System.out.printf("\r[%-50.50s] %.2f%%", Strings.repeat("#", (int) (d.getProgress() * 50)), d.getProgress() * 100);
						System.out.flush();
					}
				});
			}

			get.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(this, e);
		}
	}

	/**
	 * Sets id of a file to download
	 *
	 * @param fileId
	 * 		id of a file to download
	 */
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	/**
	 * Sets whether to listen for download process and print progressbar or not.
	 *
	 * @param listenForDownload
	 * 		whether to listen for download process and print progressbar or not.
	 */
	public void setListenForUpload(boolean listenForDownload) {
		this.listenForDownload = listenForDownload;
	}
}
