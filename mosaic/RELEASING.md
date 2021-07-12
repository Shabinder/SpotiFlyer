# Releasing

1. Update the `VERSION_NAME` in `gradle.properties` to the release version.

2. Update the `CHANGELOG.md`:
   1. Change the `Unreleased` header to the release version.
   2. Add a link URL to ensure the header link works.
   3. Add a new `Unreleased` section to the top.

3. Update the `README.md`:
   1. Change the "Download" section to reflect the new release version.
   2. Change the snapshot section to reflect the next "SNAPSHOT" version, if it is changing.
   3. Update the Kotlin version compatibility table

4. Commit

   ```
   $ git commit -am "Prepare version X.Y.X"
   ```

5. Manually release and upload artifacts
   1. Run `./gradlew -p mosaic clean publish`
   2. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
   3. If either fails, drop the Sonatype repo, fix the problem, commit, and restart this section.

6. Tag

   ```
   $ git tag -am "Version X.Y.Z" X.Y.Z
   ```

7. Update the `VERSION_NAME` in `gradle.properties` to the next "SNAPSHOT" version.

8. Commit

   ```
   $ git commit -am "Prepare next development version"
   ```

9. Push!

   ```
   $ git push && git push --tags
   ```

   This will trigger a GitHub Action workflow which will create a GitHub release.
