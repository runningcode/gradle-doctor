# Releasing

## Prerequisites

Ensure you have the following GitHub secrets configured:

- `SONATYPE_USERNAME`: Your Sonatype username
- `SONATYPE_PASSWORD`: Your Sonatype password
- `GPG_SIGNING_KEY`: Your exported ASCII-armored GPG private key
- `GPG_SIGNING_KEY_ID`: Your GPG key ID
- `GPG_SIGNING_KEY_PASSWORD`: Your GPG key password
- `GRADLE_PUBLISH_KEY`: Your Gradle Plugin Portal API key
- `GRADLE_PUBLISH_SECRET`: Your Gradle Plugin Portal API secret

## Release Process

### Option 1: Automated Release (Recommended)

- Create a local release branch from `master`

```bash
git checkout master
git pull
git checkout -b release_{{ doctor.next_release }}
```

- Update `version` in `doctor-plugin/build.gradle.kts` (remove `-SNAPSHOT`)

```kotlin
version = "{{ doctor.next_release }}"
```

- Update the current version and next version in `mkdocs.yml`:

```
extra:
  doctor:
    release: '{{ doctor.next_release }}'
    next_release: 'REPLACE_WITH_NEXT_VERSION_NUMBER'
```

- Take one last look

```
git diff
```

- Commit all local changes

```bash
git commit -am "Prepare {{ doctor.next_release }} release"
```

- Push the release branch

```bash
git push origin release_{{ doctor.next_release }}
```

- Create a pull request and merge to master

- Create a GitHub Release:

  - Go to the GitHub repository
  - Click "Releases" → "Create a new release"
  - Tag version: `v{{ doctor.next_release }}` (this will create a tag starting with `v`)
  - Release title: `{{ doctor.next_release }}`
  - Add release notes
  - Click "Publish release"

- The GitHub Action will automatically:

  - Publish to Maven Central (staging)
  - Publish to Gradle Plugin Portal

- Merge the release branch to master

```bash
git checkout master
git pull
git merge --no-ff release_{{ doctor.next_release }}
```

- Update `version` in `doctor-plugin/build.gradle.kts` (increase version and add `-SNAPSHOT`)

```kotlin
version = "REPLACE_WITH_NEXT_VERSION_NUMBER-SNAPSHOT"
```

- Commit your changes

```bash
git commit -am "Prepare for next development iteration"
```

- Push your changes

```bash
git push
```

### Option 2: Manual Release

If you prefer manual control over the release process:

- Follow steps 1-8 from Option 1 above

- Upload to Maven Central

```bash
./gradlew publishToMavenCentral --no-configuration-cache
```

- Release to Maven Central manually:

  - Login to Central Portal: [https://central.sonatype.org/](https://central.sonatype.org/)
  - Navigate to your staging repository
  - Find your staging repository and click **Release**

- Upload to Gradle Plugin Portal

```bash
./gradlew publishToGradlePlugin
```

- Follow steps 9-12 from Option 1 above

## Workflow Files

The repository uses **`.github/workflows/gradle-release.yml`** for releases, which triggers when a GitHub Release is published and uploads to staging and requires manual release on Central Portal.

## Snapshot Publishing

Snapshots are automatically published to Maven Central when changes are pushed to the `master` branch.
