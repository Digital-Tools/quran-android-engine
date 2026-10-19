# CDN source files

Mushaf page sets served from the Mizan content CDN (Cloudflare R2 bucket
`mizan-content`, Worker `mizan-cdn`), not packaged in any APK. Both Quran
engines download the selected mushaf on first launch.

To re-upload a reading (from mizan-app):

    scripts/cdn/upload_mushaf.sh <this repo>/cdn-source/readings/hafs_1405 hafs_1405

Layout per reading: `images_<width>/width_<width>/pageNNN.png` and
`images_<width>/databases/ayahinfo_<width>.db`.

`translations/` holds the zips that used to be bundled (Sahih International,
Al-Barwani, Muyassar). The engine now downloads them from android.quran.com
on first launch (`TranslationAssetsInstaller`, manifest still bundled).
