# Slower From Cache (Negative Savings)

Some tasks may be slower to pull from the Gradle Build cache.
In general, tasks that are computationally cheap but expensive on the disk are not good candidates to be cached.

### Copy tasks
A copy task should not be cached. The output of a copy task would need to be compressed and then stored in
the build cache. To restore the copy task from the cache, Gradle would decompress the outputs and then rewrite them in
the output directories. Simply performing the copy again would simply move the files from their original location without
need to compress and then decompress them.

### Non compressible outputs
Tasks which generate large output which is not easily compressible may also not be good candidates for the build cache.
Tasks that move compressed images like pngs, jpegs, or precomputed libraries are not good caching candidates.

### Disabling caching

=== "Groovy"
    ``` groovy
    tasks.named("<taskname>").configure {
        outputs.cacheIf { false }
    }
    ```
=== "Kotlin"
    ``` kotlin
    tasks.named('<taskname>').configure {
        outputs.cacheIf { false }
    }
    ```

### Disabling remote caching
Some Gradle tasks may be beneficial to cache locally but not on CI. There is currently no mechanism to only disable
remote caching of tasks. A workaround is to disable the cache entry being placed in to the remote build cache.

=== "Groovy"
    ``` groovy
    tasks.named('<taskname>').configure {
        outputs.cacheIf { !isCI }
    }
    ```
=== "Kotlin"
    ``` kotlin
    tasks.named("<taskname>").configure {
        outputs.cacheIf { !isCI }
    }
    ```

See [Remote Build Cache Benchmark](../remote-cache) for more information on estimating whether it is beneficial
to remotely cache tasks.
