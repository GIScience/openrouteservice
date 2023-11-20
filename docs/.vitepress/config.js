import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    title: "openrouteservice backend documentation",
    description: "openrouteservice backend documentation",
    base: "/openrouteservice/",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        siteTitle: false,
        logo: {
            src: '/openrouteservice.png',
            alt: 'openrouteservice logo',
        },
        search: {
            provider: 'local'
        },
        nav: [
            {text: 'Homepage', link: 'https://openrouteservice.org'},
            {text: 'Forum', link: 'https://ask.openrouteservice.org'},
            {text: 'API Playground', link: 'https://openrouteservice.org/dev/#/api-docs'},
        ],
        sidebar: [
            {
                text: 'Home', link: '/',
                items: [
                    {text: 'Getting Started', link: '/getting-started'},
                    {text: 'Installation and Usage', link: 'installation/Installation-and-Usage'}
                ]
            }
        ],
        socialLinks: [
            {icon: 'github', link: 'https://github.com/GIScience/openrouteservice'}
        ],
        footer: {
            message: '<a href="https://openrouteservice.org/">openrouteservice</a> is part of <a href="https://heigit.org/">HeiGIT gGmbH</a> and Universität Heidelberg <a href="https://www.geog.uni-heidelberg.de/gis/index_en.html">GIScience</a> research group. | <a href="https://heigit.org/imprint/">Imprint</a>'
        }
    }
})
