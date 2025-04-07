1. Check that
    - all relevant PRs are merged
    - documentation is up-to-date
    - CHANGELOG is up-to-date
    - if major version release: release notes for announcement/blog is ready
2. Select the new release number: We use a number consisting of the parts `<major>.<minor>.<patch>`, in the following abbreviated as `X.Y.Z`. We change the patch version, if the release only contains bugfixes. Releases with additions like new features or not mandatory additional API request parameters etc. are minor releases. If there are breaking changes with require changes e.g. in existing configuration files or API requests, the major version is incremented.
3. Create a Release branch named `releases/vX.Y.Z` and
   a. Update `version` property in [package.json](package.json) to new release number
   b. Update CHANGELOG.md as follows:
    1. Change unreleased to new release number
    2. Add today's Date
    3. Change unreleased link to compare new release:
       [unreleased]: https://github.com/GIScience/openrouteservice/compare/vnew...HEAD
    4. Add new compare link below
       [new]: https://github.com/GIScience/openrouteservice/compare/vlast...vnew
    5. Double check issue links are valid
    6. Add [unreleased] section with all subsections as above
       b. Update version numbers in POM using
       `mvn versions:set -DnewVersion=X.Y.Z`
       or setting it manually in the main and all child POMs
       c. Commit changes as chore or build, and push
       d. Open and merge PR as
       `chore: release vX.Y.Z`
3. After the branch `release/vX.Y.Z` is merged to main, draft a new release on Github.
   Generate release notes automagically and curate by hand.
   This also creates the new `vX.Y.Z` tag.
4. Check that the following assets exists, after the workflows have finished:
    - docker-compose.yml (using the new version)
    - ors-config.env
    - ors-config.yml
    - ors.jar
    - ors.war
    - Source code (zip)
    - Source code (tar.gz)
5. Check that docker images were created correctly:
    - `vX.Y.Z` should now exist
    - `latest` should point to the new image
    - `vX` should point to the new image
    - This is currently set up for openrouteservice/openrouteservice. To copy the docker images also to heigit/openrouteservice, do the following:
    ```shell
    docker pull openrouteservice/openrouteservice:vX.Y.Z
    docker tag openrouteservice/openrouteservice:vX.Y.Z heigit/openrouteservice:vX.Y.Z
    docker push heigit/openrouteservice:vX.Y.Z
    docker tag heigit/openrouteservice:vX.Y.Z heigit/openrouteservice:latest
    docker push heigit/openrouteservice:latest
    ```
6. Change latest and vX tags:
   a. Delete tags on github
   b. Delete tags locally:
    - git tag -d latest
    - git tag -d vX
      c. Re-create tags locally on the new main HEAD
    - git tag latest
    - git tag vX
      d. Push new tags
    - git push origin tag latest
    - git push origin tag vX
7. Update version in POMs to X.Y.Z-SNAPSHOT using
   mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
8. Check whether outreach, announcement, … is necessary and do so.
