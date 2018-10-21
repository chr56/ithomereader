# ITHomeReader

A 3rd-party [ITHome](https://www.ithome.com) App.

*Note: Currently there might be lots of issues in the code, but the app should work well. I will keep improving the code when I have free time.*

## Building

Modify `app/build.gradle` , remove `apply from: 'config.gradle'` or create your own `config.gradle` like below. It is used to define the update check URL.

    ext {
        updateUrl = 'YOUR_OWN_UPDATE_URL_HERE'
    }
    
Note: If you remove the config, you'll also need to remove or edit `manifestPlaceholders` for normal flavor.

Then build the app using Android Studio.

### Example of update info JSON

    {  
        "versionCode":122,
        "version":"4.3.16",
        "log":"What's new in this version...",
        "url":"https://example.com/new_version.apk"
    }
