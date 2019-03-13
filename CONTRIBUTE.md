# Contribution Guidelines

<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Contribution Guidelines](#contribution-guidelines)
	- [Issues](#issues)
		- [Technical issues](#technical-issues)
		- [Routing issues](#routing-issues)
		- [Feature requests](#feature-requests)
	- [Pull Requests](#pull-requests)
		- [Pull request guidelines](#pull-request-guidelines)
		- [Contributing code](#contributing-code)

<!-- /TOC -->

**Firstly, thanks for using the ORS and contributing to making it better for everyone.**

OpenRouteService has been around for over ten years, and as such it has seen many changes and adoption of new methodologies and techniques, as well as the coming and going of multiple developers. To try and keep the OpenRouteService codebase clean and stable, there are a few guidelines that we try to follow.

## Issues

We accept technical issues, routing issues and feature requests. Common questions should be asked at [ask.openrouteservice.org](https://ask.openrouteservice.org).

Please search for issues before creating [a new one](https://github.com/GIScience/openrouteservice/issues/new).

### Technical issues

If you encounter a bug, please make sure to be as descriptive as possible, i.e.:

- operating system (in case of self-hosting)
- request URL (incl. parameters if POST)
- expected outcome
- actual outcome (e.g. JSON output)
- fenced code around the bug, if known

### Routing issues

If you encounter weird or unexpected behavior during routing, please make sure to rule out expected behavior based on road attributes from e.g. [openstreetmap.org](https://openstreetmap.org/query), such as unexpected one-way streets or access restrictions. Find an overview of our tagging filters for all transportation profiles in our [wiki](https://github.com/GIScience/openrouteservice/wiki/Tag-Filtering).

Information to include:

- request URL (incl. parameters if POST)
- expected outcome
- actual outcome (e.g. JSON output)
- screenshots where applicable (e.g. from our [maps](https://maps.openrouteservice.org))

### Feature requests

Here you can be creative, but still descriptive. Make sure to describe the current behavior, your desired behavior and **to give actual use cases**. That makes it whole easier for us to prioritize.

## Pull Requests

We :heart: pull requests! We also aspire to make our commit history cleaner and more sustainable, benefiting our contributors and us maintainers.

### Pull request guidelines

We'd like all pull requests to adhere to the following rules:

- a PR has to **close an issue**. If there is none yet for yours, please [create one](https://github.com/GIScience/openrouteservice/issues/new)
- branch off [development](https://github.com/GIScience/openrouteservice/tree/development)
- name your branch according to `<[hotfix/bugfix/feat/algo]>/<issue#>-<purpose>`, e.g. `[feat]/#381-simplify_geometry`
- if you introduce new functions/classes, write unit or API tests
- **limit the number of commits to a minimum**, i.e. use [`git commit --amend [--no-edit]`](https://www.atlassian.com/git/tutorials/rewriting-history#git-commit--amend)
- use meaningful commit messages, e.g. `commit -m "[feat] include geometry_simplify in API and core code"`
- if your branch needs an update from its base branch, use [`rebase`](https://blog.algolia.com/master-git-rebase/#rebasing-on-the-base-branch), e.g.

```
git checkout my-new-feat
git stash
git rebase development
git stash pop
```

Be extra careful using `rebase` commands when collaborating on a branch with other people.

**Don't merge** `development` branch into your feature branch.

### Contributing code

For a short explanation on how to setup, deploy and test **openrouteservice** locally for development, see our [wiki entry](https://github.com/GIScience/openrouteservice/wiki/Contributing-Code).

Though there are no concrete rules for code that is contributed, we have a few general styles that should be adopted:

- **Descriptive names** for variables, methods and classes
- **Minimal in-line comments**: code should be self-explanatory. **Never** use comments to comment out blocks of code
- **Use JDoc** docstrings to provide context for methods and classes
- **Unit tests!!**
- **API test** when adding API functionality in the corresponding [`openrouteservice-api-tests`](https://github.com/GIScience/openrouteservice/tree/master/openrouteservice-api-tests) subproject
- Keep **methods modular**: rather short and singular functionality than 100s line of code
