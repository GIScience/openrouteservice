# Contribution Guidelines

<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Contribution Guidelines](#contribution-guidelines)
	- [Issues](#issues)
	- [Pull Requests](#pull-requests)
		- [Pull request guidelines](#pull-request-guidelines)
		- [Contributing code](#contributing-code)

<!-- /TOC -->

**Firstly, thanks for using the ORS and contributing to making it better for everyone.**

Openrouteservice has been around for over ten years, and as such it has seen many changes and adoption of new
methodologies and techniques, as well as the coming and going of multiple developers.
To try and keep the openrouteservice codebase clean and stable, there are a few guidelines that we try to follow.

## Issues

Please search for existing issues before creating a new one!
Choose the [most fitting issue template](https://github.com/GIScience/openrouteservice/issues/new) and provide the
required information.
For common questions use [our Community Forum](https://ask.openrouteservice.org) instead.

## Pull Requests

We :heart: pull requests! We also aspire to make our commit history cleaner and more sustainable, benefiting our contributors and us maintainers.

### Pull request guidelines

We'd like all pull requests to adhere to the following rules:

- a PR has to **close an issue**. If there is none yet for yours, please [create one](https://github.com/GIScience/openrouteservice/issues/new)
- branch off [main](https://github.com/GIScience/openrouteservice/tree/main)
- name your branch according to `<[hotfix/bugfix/feat/algo]>/<issue#>-<purpose>`, e.g. `feat/#381-simplify_geometry`
- if you introduce new functions/classes, write unit or API tests
- **limit the number of commits to a minimum**, i.e. use [`git commit --amend [--no-edit]`](https://www.atlassian.com/git/tutorials/rewriting-history#git-commit--amend)
- use meaningful commit messages, e.g. `commit -m "Include geometry_simplify in API and core code"`
- if your branch needs an update from its base branch, use [`rebase`](https://blog.algolia.com/master-git-rebase/#rebasing-on-the-base-branch), e.g.

```
git checkout feat/#123-add-great-feature
git stash
git rebase main
git stash pop
```

Be extra careful using `rebase` commands when collaborating on a branch with other people.

**Don't merge** `main` branch into your feature branch.

### Contributing code

For a short explanation on how to set up, deploy and test **openrouteservice** locally for development, see our [installation instructions](https://GIScience.github.io/openrouteservice/run-instance/installation/building-from-source).

Though there are no concrete rules for code that is contributed, we have a few general styles that should be adopted:

- **Descriptive names** for variables, methods and classes
- **Minimal in-line comments**: code should be self-explanatory. **Never** use comments to comment out blocks of code
- **Use JDoc** docstrings to provide context for methods and classes
- **Unit tests!!**
- **API test** when adding API functionality in the corresponding `apitests` package
- Keep **methods modular**: rather short and singular functionality than 100s line of code
