1. Check that
   - all relevant PRs are merged
   - documentation is up-to-date
   - CHANGELOG is up-to-date
   - if major version release: release notes for announcement/blog is ready
2. Create a Release branch named releases/vX.X.X and
   a. Update CHANGELOG.md as follows:
      1. Change unreleased to new release number
      2. Add today's Date
      3. Change unreleased link to compare new release:
         [unreleased]: https://github.com/GIScience/openrouteservice/compare/vnew...HEAD
      4. Add new compare link below
         [new]: https://github.com/GIScience/openrouteservice/compare/vlast...vnew
      5. Double check issue links are valid
      6. Add [unreleased] section with all subsections as above
   b. Update version numbers in POM using
      mvn versions:set -DnewVersion=X.X.X
      or setting it manually in the main and all child POMs
   c. Commit changes as chore or build, and push
   d. Open and merge PR as
      chore: release vX.X.X
3. Draft a new release on Github
   Generate release notes automagically and curate by hand
   This also creates the new vX.X.X tag.
4. Check that the following assests exists, after the workflows have finished:
   - docker-compose.yml (using the new version)
   - ors-config.env
   - ors-config.yml
   - ors.jar
   - ors.war
   - Source code (zip)
   - Source code (tar.gz)
5. Check that docker images were created correctly:
   - vX.X.X should now exist
   - latest should point to the new image
   - v<major> should point to the new image
6. Change latest and v<major> tags:
   a. Delete tags on github
   b. Delete tags locally:
      - git tag -d latest
      - git tag -d v<major>
   c. Re-create tags locally on the new main HEAD
      - git tag latest
      - git tag v<major>
   d. Push new tags
      - git push origin tag latest
      - git push origin tag v<major>
7. Update version in POMs to X.Y.Z-SNAPSHOT using
   mvn versions:set -DnewVersion=X.Y.Z-SNAPSHOT
8. Check whether outreach, announcement, … is necessary and do so.
