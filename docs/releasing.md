# Releasing

* Create a local release branch from `master`
```bash
git checkout master
git pull
git checkout -b release_{{ doctor.next_release }}
```

* Update `version` in `doctor-plugin/build.gradle.kts` (remove `-SNAPSHOT`)
```kotlin
version = "{{ doctor.next_release }}"
```

* Update the current version and next version in `mkdocs.yml`:
```
extra:
  doctor:
    release: '{{ doctor.next_release }}'
    next_release: 'REPLACE_WITH_NEXT_VERSION_NUMBER'
```

* Take one last look
```
git diff
```

* Commit all local changes
```
git commit -am "Prepare {{ doctor.next_release }} release"
```

* Create a tag and push it
```bash
git tag v{{ doctor.next_release }}
git push origin v{{ doctor.next_release }}
```

* Upload to Maven Central
``` bash
./gradlew publishToMavenCentral -Dorg.gradle.internal.publish.checksums.insecure=true
```
* Upload to Gradle Plugin Portal
```bash
./gradlew publishToGradlePlugin
```

* Release to Maven Central
    * Login to Sonatype OSS Nexus: [https://oss.sonatype.org/](https://oss.sonatype.org/)
    * Click on **Staging Repositories**

* Merge the release branch to master
```
git checkout master
git pull
git merge --no-ff release_{{ doctor.next_release }}
```
* Update `version` in `buildSrc/build.gradle.kts` (increase version and add `-SNAPSHOT`)
```kotlin
version = "REPLACE_WITH_NEXT_VERSION_NUMBER-SNAPSHOT"
```

* Commit your changes
```
git commit -am "Prepare for next development iteration"
```

* Push your changes
```
git push
```
