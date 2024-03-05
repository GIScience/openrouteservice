import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    title: "openrouteservice backend documentation",
    description: "openrouteservice backend documentation",
    base: "/openrouteservice/",
    head: [['link', {rel: 'icon', href: '/openrouteservice/ors_fav.png'}]],
    cleanUrls: true,
    ignoreDeadLinks: 'localhostLinks',
    markdown: {
        languageAlias: {
            'jsonpath': 'json'
        }
    },
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        siteTitle: false,
        logo: {
            light: '/openrouteservice.png',
            dark: '/openrouteservice_dark.png',
            alt: 'openrouteservice logo'
        },
        search: {
            provider: 'local'
        },
        outline: {
            level: [2, 4]
        },
        lastUpdated: {
            text: 'Updated at',
            formatOptions: {
                dateStyle: 'medium',
                timeStyle: 'short'
            }
        },
        editLink: {
            pattern: 'https://github.com/GIScience/openrouteservice/issues/new?labels=documentation+%3Abook%3A&template=docs.yml',
            text: 'Suggest an improvement'
        },
        nav: [
            {text: 'Homepage', link: 'https://openrouteservice.org'},
            {text: 'API Playground', link: 'https://openrouteservice.org/dev/#/api-docs'},
            {text: 'Forum', link: 'https://ask.openrouteservice.org'},
        ],
        sidebar: [
            {
                text: 'Home', link: '/',
                items: [
                    {text: 'Getting Started', link: '/getting-started'},
                    {
                        text: 'API Reference', collapsed: true, link: '/api-reference/',
                        items: [
                            {
                                text: 'Endpoints', collapsed: true, link: '/api-reference/endpoints/',
                                items: [
                                    {
                                        text: 'Directions', collapsed: true, link: '/api-reference/endpoints/directions/',
                                        items: [
                                            {text: 'Requests and Return Types', link: '/api-reference/endpoints/directions/requests-and-return-types'},
                                            {text: 'Routing Options', link: '/api-reference/endpoints/directions/routing-options'},
                                            {
                                                text: 'Extra info', collapsed: true, link: '/api-reference/endpoints/directions/extra-info/',
                                                items: [
                                                    {text: 'Steepness IDs', link: '/api-reference/endpoints/directions/extra-info/steepness'},
                                                    {text: 'Surface IDs', link: '/api-reference/endpoints/directions/extra-info/surface'},
                                                    {text: 'Category IDs', link: '/api-reference/endpoints/directions/extra-info/waycategory'},
                                                    {text: 'Type IDs', link: '/api-reference/endpoints/directions/extra-info/waytype'},
                                                    {text: 'Difficulty IDs', link: '/api-reference/endpoints/directions/extra-info/trail-difficulty'},
                                                    {text: 'Restriction IDs', link: '/api-reference/endpoints/directions/extra-info/road-access-restrictions'},
                                                    {text: 'Country IDs', link: '/technical-details/country-list'},
                                                ]
                                            },
                                            {text: 'Route Attributes', link: '/api-reference/endpoints/directions/route-attributes'},
                                            {text: 'Geometry Decoding', link: '/api-reference/endpoints/directions/geometry-decoding'},
                                            {text: 'Instruction Types', link: '/api-reference/endpoints/directions/instruction-types'},
                                        ]
                                    },
                                    {text: 'Isochrones', link: '/api-reference/endpoints/isochrones/'},
                                    {text: 'Matrix', link: '/api-reference/endpoints/matrix/'},
                                    {text: 'Snapping (not live)', link: '/api-reference/endpoints/snapping/'},
                                    {text: 'Export (not live)', link: '/api-reference/endpoints/export/'},
                                    {text: 'Health (not live)', link: '/api-reference/endpoints/health/'},
                                    {text: 'Status (not live)', link: '/api-reference/endpoints/status/'},
                                    {text: 'POI', link: '/api-reference/endpoints/poi/'},
                                    {text: 'Elevation', link: '/api-reference/endpoints/elevation/'},
                                    {text: 'Geocoder', link: '/api-reference/endpoints/geocoder/'},
                                    {text: 'Optimization', link: '/api-reference/endpoints/optimization/'},
                                ]
                            },
                            {text: 'Error Codes', link: '/api-reference/error-codes'},
                        ]
                    },
                    {
                        text: 'Run ORS instance', collapsed: true, link: '/run-instance/',
                        items: [
                            {text: 'System Requirements', link: '/run-instance/system-requirements'},
                            {text: 'Data', link: '/run-instance/data'},
                            {text: 'Running with Docker', link: '/run-instance/running-with-docker'},
                            {text: 'Running JAR', link: '/run-instance/running-jar'},
                            {text: 'Running WAR', link: '/run-instance/running-war'},
                            {text: 'Building from Source', link: '/run-instance/building-from-source'},
                            {
                                text: 'Configuration', collapsed: true, link: '/run-instance/configuration/',
                                items: [
                                    {text: 'Spring Properties', link: '/run-instance/configuration/spring/', collapsed:true,
                                        items: [
                                            {text: 'server', link: '/run-instance/configuration/spring/server.md'},
                                            {text: 'logging', link: '/run-instance/configuration/spring/logging.md'},
                                        ]
                                    },
                                    {text: 'ORS Properties', collapsed:true,
                                        items: [
                                            {text: 'endpoints', link: '/run-instance/configuration/ors/endpoints/', collapsed: true,
                                                items: [
                                                    {text: 'defaults', link: '/run-instance/configuration/ors/endpoints/defaults.md'},
                                                    {text: 'routing', link: '/run-instance/configuration/ors/endpoints/routing.md'},
                                                    {text: 'matrix', link: '/run-instance/configuration/ors/endpoints/matrix.md'},
                                                    {text: 'isochrones', link: '/run-instance/configuration/ors/endpoints/isochrones.md'},
                                                    {text: 'snap', link: '/run-instance/configuration/ors/endpoints/snap.md'},
                                                ]
                                            },
                                            {text: 'engine', link: '/run-instance/configuration/ors/engine/', collapsed: true,
                                                items: [
                                                    {text: 'profiles', link: '/run-instance/configuration/ors/engine/profiles.md'},
                                                    {text: 'elevation', link: '/run-instance/configuration/ors/engine/elevation.md'},
                                                ]
                                            },
                                            {text: 'cors', link: '/run-instance/configuration/ors/cors/'},
                                            {text: 'messages', link: '/run-instance/configuration/ors/messages/'}
                                        ]
                                    },
                                    {text: 'JSON config (deprecated)', link:'/run-instance/configuration/json.md'}
                                ]
                            },
                        ]
                    },
                    {
                        text: 'Contributing', collapsed: true, link: '/contributing/',
                        items: [
                            {text: 'Backend documentation', link: '/contributing/backend-documentation'},
                            {text: 'Contribution guidelines', link: 'https://github.com/GIScience/openrouteservice/blob/main/CONTRIBUTE.md'},
                            {text: 'Contributing translations', link: '/contributing/contributing-translations'},
                        ]
                    },
                    {
                        text: 'Technical details', collapsed: true, link: '/technical-details/',
                        items: [
                            {text: 'Country List', link: '/technical-details/country-list'},
                            {text: 'Travel Speeds', link: '/technical-details/travel-speeds/',
                                items: [
                                    {text: 'Country Speeds', link: '/technical-details/travel-speeds/country-speeds.md'},
                                    {text: 'Tracktype Speeds', link: '/technical-details/travel-speeds/tracktype-speeds.md'},
                                    {text: 'Waytype Speeds', link: '/technical-details/travel-speeds/waytype-speeds.md'},
                                    {text: 'Surface Speeds', link: '/technical-details/travel-speeds/surface-speeds.md'},
                                ]
                            },
                            {text: 'Tag Filtering', link: '/technical-details/tag-filtering'}
                        ]
                    },
                    {text: 'FAQ', link: '/frequently-asked-questions'}
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
