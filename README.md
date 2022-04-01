# Anytype.io - Mobile

### Build project

##### Prerequisites

- Download `anytype/anytype-android` (private) repository from Github
  
- Create `github.properties` file in root project folder:

```
gpr.usr=GITHUB_USER_ID
gpr.key=GITHUB_TOKEN
```

- Create `apikeys.properties` file in root project folder:

```
amplitude.debug="AMPLITUDE_DEBUG_KEY"
amplitude.release="AMPLITUDE_RELEASE_KEY"
```

Then build project.

### Setup your Firebase account for Anytype

We're using *Firebase App Distribution* + *Firebase Crashlytics*. We have two separate projects: one for `debug` builds (which we distribute mostly for our Q&A team), another one for `release` builds.

### Install the latest Anytype Android release: 

From [Google Play](https://play.google.com/store/apps/details?id=com.anytypeio.anytype).

Or [download](https://download.anytype.io/) an apk for your device architecture from our website.

### Join our community & telegram channels

[Official Anytype community](https://community.anytype.io/).

[Follow what community writes about our Android client](https://community.anytype.io/tag/Android).

[Join our Android Testers telegram channel](https://t.me/+vEb8COFY7rY5Mzli).

### Useful links

[Our tech change log](https://github.com/anytypeio/android-anytype/blob/develop/CHANGELOG.md).

[Anytype Android app dependencies](https://github.com/anytypeio/android-anytype/blob/develop/dependencies.gradle).




