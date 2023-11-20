import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    title: "openrouteservice backend documentation",
    description: "openrouteservice backend documentation",
    base: "/openrouteservice/",
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        logo: {
            src: '/openrouteservice.png',
            alt: 'openrouteservice logo',
        },
        nav: [
            {text: 'Home', link: '/'},
            {text: 'Examples', link: '/markdown-examples'}
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
