# gradle-backup-plugin

This [Gradle](http://gradle.org/) plugin allows you to automate you small non-production backups and upload them into various clouds.

Currently it can upload backups to:

+ [Google Drive](https://www.google.com/drive/)

You can create [ZIP](https://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.Zip.html) and [TAR](https://gradle.org/docs/current/dsl/org.gradle.api.tasks.bundling.Tar.html) archives using awesome Gradle's out-of-the features! And if you're really paranoid you can encrypt archives with your favourite Java encryption library before the uploading.
