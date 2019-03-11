### Pull Request Checklist
<!--- Please make sure you have completed the following items BEFORE submitting a pull request (put an x in each box when you have checked you have done them): -->
- [ ] 1. I have [**rebased**](https://github.com/GIScience/openrouteservice/blob/master/CONTRIBUTE.md#pull-request-guidelines) the latest version of the development branch into my feature branch and all conflicts have been resolved.
- [ ] 2. I have added information about the change/addition to functionality to the CHANGELOG.md file under the [Unreleased] heading.
- [ ] 3. I have documented my code using JDocs tags.
- [ ] 4. I have removed unnecessary commented out code, imports and System.out.println statements.
- [ ] 5. I have written JUnit tests for any new methods/classes and ensured that they pass.
- [ ] 6. I have created API tests for any new functionality exposed to the API.
- [ ] 7. If changes/additions are made to the app.config file, I have added these to the app.config wiki page on github along with a short description of what it is for, and documented this in the Pull Request (below).
- [ ] 8. I have built graphs with my code of the Heidelberg.osm.gz file and run the api-tests with all test passing
- [ ] 9. I have referenced the Issue Number in the Pull Request (if the changes were from an issue).
- [ ] 10. For new features or changes involving building of graphs, I have tested on a larger dataset (at least Germany) and the graphs build without problems (i.e. no out-of-memory errors).
- [ ] 11. For new features or changes involving the graphbuilding process (i.e. changing encoders, updating the importer etc.), I have generated longer distance routes for the affected profiles with different options (avoid features, max weight etc.) and compared these with the routes of the same parameters and start/end points generated from the current live ORS. If there are differences then the reasoning for these **MUST** be documented in the pull request.
- [ ] 12. I have written in the Pull Request information about the changes made including their intended usage and why the change was needed.

Fixes # .

### Information about the changes
- Key functionality added:
- Reason for change:

### Examples and reasons for differences between live ORS routes and those generated from this pull request
-

### Required changes to app.config (if applicable)
-
