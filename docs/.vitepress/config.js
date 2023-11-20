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
                    {
                        text: 'Installation and Usage', collapsed: true, link: '/installation/Installation-and-Usage',
                        items: [
                            {text: 'Running with Docker', link: '/installation/Running-with-Docker'},
                            {text: 'Building from Source', link: '/installation/Building-from-Source'},
                            {text: 'System Requirements', link: '/installation/System-Requirements'},
                            {text: 'Configuration', link: '/installation/Configuration'},
                            {text: 'Advanced Docker Setup', link: '/installation/Advanced-Docker-Setup'}
                        ]
                    },
                    {text: 'Data', link: '/Data'},
                    {
                        text: 'Contributing', collapsed: true, link: '/contributing/Contributing',
                        items: [
                            {text: 'Contributing Translations', link: '/contributing/Contributing-Translations'}
                        ]
                    },
                    {
                        text: 'Documentation', collapsed: true, link: '/documentation/Documentation',
                        items: [
                            {text: 'Tag Filtering', link: '/documentation/Tag-Filtering'},
                            {
                                text: 'Travel Speeds',
                                collapsed: true,
                                link: '/documentation/travel-speeds/Travel-Speeds',
                                items: [
                                    {text: 'Waytype Speeds', link: '/documentation/travel-speeds/Waytype-Speeds'},
                                    {text: 'Surface Speeds', link: '/documentation/travel-speeds/Surface-Speeds'},
                                    {text: 'Tracktype Speeds', link: '/documentation/travel-speeds/Tracktype-Speeds'},
                                    {text: 'Country Speeds', link: '/documentation/travel-speeds/Country-Speeds'}
                                ]
                            },
                            {text: 'Route Attributes', link: '/documentation/Route-Attributes'},
                            {
                                text: 'Routing Options',
                                collapsed: true,
                                link: '/documentation/routing-options/Routing-Options',
                                items: [
                                    {text: 'Examples', link: '/documentation/routing-options/Examples'},
                                    {text: 'Country List', link: '/documentation/routing-options/Country-List'},
                                ]
                            },
                            {text: 'Instruction Types', link: '/documentation/Instruction-Types'},
                            {
                                text: 'Extra Info', collapsed: true, link: '/documentation/extra-info/Extra-Info',
                                items: [
                                    {text: 'Steepness', link: '/documentation/extra-info/Steepness'},
                                    {text: 'Surface', link: '/documentation/extra-info/Surface'},
                                    {text: 'Waycategory', link: '/documentation/extra-info/Waycategory'},
                                    {text: 'Waytype', link: '/documentation/extra-info/Waytype'},
                                    {text: 'Trail Difficulty', link: '/documentation/extra-info/Trail-Difficulty'},
                                    {
                                        text: 'Road Access Restrictions',
                                        link: '/documentation/extra-info/Road-Access-Restrictions'
                                    }
                                ]
                            },
                            {text: 'Geometry Decoding', link: '/documentation/Geometry-Decoding'},
                            {text: 'Structured Geocoding Query', link: '/documentation/Structured-Geocoding-Query'},
                            {text: 'Matrix Response', link: '/documentation/Matrix-Response'},
                            {text: 'Places Request and Response', link: '/documentation/Places-Request-and-Response'},
                            {text: 'Geocoding Response', link: '/documentation/Geocoding-Response'},
                            {text: 'Error Codes', link: '/documentation/Error-Codes'},
                        ]
                    },
                    {text: 'FAQ', link: '/Frequently-Asked-Questions'}
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
