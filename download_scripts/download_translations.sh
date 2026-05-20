#!/bin/bash
set -e

mkdir -p app/src/main/assets/translations
cd app/src/main/assets/translations

# Fetch translations list
echo "Fetching translations API..."
curl -sL "https://android.quran.com/data/translations.php?v=5" -o translations.json

# Parse and download selected languages (en, ar, fr, pt, sw, af)
# For simplicity, we'll pick one common translator per language from the API response

jq -c '.data[] | select(.languageCode == "en" and .translator == "Sahih International" or .languageCode == "fr" and .translator == "Muhammad Hamidullah" or .languageCode == "pt" and .translator == "Samir El-Hayek" or .languageCode == "sw" and .translator == "Ali Muhsin Al-Barwani" or .languageCode == "ar" and .translator == "Tafsir al-Jalalayn")' translations.json > selected.json

cat selected.json | while read item; do
    id=$(echo "$item" | jq -r '.id')
    fileName=$(echo "$item" | jq -r '.fileName')
    fileUrl=$(echo "$item" | jq -r '.fileUrl')
    
    zipName="${fileName%.*}.zip"
    echo "Downloading $fileName ($fileUrl)..."
    curl -sL "$fileUrl" -o "$zipName"
done

echo "Downloaded translations successfully!"
