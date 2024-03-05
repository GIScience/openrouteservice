# Setting up the backend documentation

The backend documentation is built using [vitepress](https://vitepress.dev/guide/getting-started).

Requirements:
- [node version 20](https://nodejs.org/en/download)

## Set up locally

To run a development version with hot reload locally, run

```bash
npm install
npm run docs:dev
```

To build the static files to `docs/.vitepress/dist` and preview the production version locally, use

```bash
npm run build-preview
```

::: info
For Markdown links to resolve properly in your IDE (e.g. Intellij), right-click the `docs` folder and 
mark it as 'Sources Root'.
:::

## Configuration and Theme

Configuration of the site (e.g. sidebar, navigation, …) is in `docs/.vitepress/config.js`.
Follow the [vitepress reference](https://vitepress.dev/reference/site-config) for changes to that.

The custom theme is in `docs/.vitepress/theme`.
See the [vitepress guide](https://vitepress.dev/guide/extending-default-theme) for details.

## Deployed version

The documentation is deployed via a GitHub Action in `.github/workflows/deploy-docs.yml`
using [GitHub Pages](https://pages.github.com/)
on [giscience.github.io/openrouteservice](https://giscience.github.io/openrouteservice/).
