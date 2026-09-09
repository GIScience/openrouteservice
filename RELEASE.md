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
       `./mvnw versions:set -DnewVersion=X.Y.Z`
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
   ./mvnw versions:set -DnewVersion=X.Y.Z-SNAPSHOT
8. Check whether outreach, announcement, … is necessary and do so.

## If a release fails its vulnerability gate

If a finding has no upstream fix yet (or is a false positive / not reachable in our build), record
it as an accepted risk in `openvex.json` at the repo root instead of ignoring the
whole gate:

1. Generate a CycloneDX SBOM of the image and copy the exact `purl` for the affected component -
   a near-miss purl silently suppresses nothing:
   ```
   trivy image --format cyclonedx local/openrouteservice:test-slim | jq '.components[] | select(.name=="<lib>")'
   ```
2. Add one statement per `(CVE, subcomponent)` pair.
3. `status: "not_affected"` requires a `justification` from Trivy's/OpenVEX's fixed enum (e.g.
   `vulnerable_code_not_in_execute_path`, `vulnerable_code_not_present`). Add an `impact_statement`
   explaining why.
4. Set `timestamp` to the review date. VEX has no expiry of its own, so nothing forces a suppression to lapse.

Verify a statement actually suppresses the finding before relying on it, with `--show-suppressed`:
```
trivy image --scanners vuln,secret --vex openvex.json --show-suppressed \
  local/openrouteservice:test-slim
```
The finding should appear under "Suppressed Vulnerabilities" with your statement's justification.
