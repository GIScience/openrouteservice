import {withMermaid} from "vitepress-plugin-mermaid";

// https://vitepress.dev/reference/site-config
export default withMermaid({
    mermaid: {
        // refer https://mermaid.js.org/config/setup/modules/mermaidAPI.html#mermaidapi-configuration-defaults for options
    },
    // optionally set additional config for plugin itself with MermaidPluginConfig
    mermaidPlugin: {
        // class: "mermaid my-class", // set additional css classes for parent container
    },

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
	// undocumented options for NotFound-Page
	notFound: {
		title: 'MAYBE YOU CLICKED AN OLD LINK?',
		quote: 'We recently reworked most of our documentation. You probably ended up here by clicking an old link somewhere, e.g. in the forum. Let us know how you got here and we\'ll fix it. Click the link below and check "Getting Started" - this will help you figure out where to find what you came for.',
		linkText: 'Documentation Home',
	},
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
                                    {text: 'Snapping', link: '/api-reference/endpoints/snapping/'},
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
                                    {
                                        text: 'How to configure',
                                        link: '/run-instance/configuration/how-to-configure.md'
                                    },
                                    {
                                        text: 'What to configure',
                                        link: '/run-instance/configuration/what-to-configure.md'
                                    },
                                    {text: 'server', link: '/run-instance/configuration/server.md'},
                                    {text: 'logging', link: '/run-instance/configuration/logging.md'},
                                    {
                                        text: 'ors.endpoints',
                                        link: '/run-instance/configuration/endpoints/',
                                        collapsed: true,
                                        items: [
                                            {
                                                text: 'defaults',
                                                link: '/run-instance/configuration/endpoints/defaults.md'
                                            },
                                            {text: 'routing', link: '/run-instance/configuration/endpoints/routing.md'},
                                            {text: 'matrix', link: '/run-instance/configuration/endpoints/matrix.md'},
                                            {
                                                text: 'isochrones',
                                                link: '/run-instance/configuration/endpoints/isochrones.md'
                                            },
                                            {text: 'snap', link: '/run-instance/configuration/endpoints/snap.md'},
                                        ]
                                    },
                                    {
                                        text: 'ors.engine',
                                        link: '/run-instance/configuration/engine/',
                                        collapsed: true,
                                        items: [
                                            {
                                                text: 'graph_management',
                                                link: '/run-instance/configuration/engine/graph-management.md'
                                            },
                                            {
                                                text: 'elevation',
                                                link: '/run-instance/configuration/engine/elevation.md'
                                            },
                                            {
                                                text: 'profiles',
                                                link: '/run-instance/configuration/engine/profiles/',
                                                collapsed: true,
                                                items: [
                                                    {
                                                        text: 'build',
                                                        link: '/run-instance/configuration/engine/profiles/build.md'
                                                    },
                                                    {
                                                        text: 'repo',
                                                        link: '/run-instance/configuration/engine/profiles/repo.md'
                                                    },
                                                    {
                                                        text: 'service',
                                                        link: '/run-instance/configuration/engine/profiles/service.md'
                                                    },
                                                ]
                                            },
                                        ]
                                    },
                                    {text: 'ors.cors', link: '/run-instance/configuration/cors/'},
                                    {text: 'ors.messages', link: '/run-instance/configuration/messages/'},
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
                            {text: 'Tag Filtering', link: '/technical-details/tag-filtering'},
                            {text: 'Graph Management', link: '/technical-details/graph-repo-client/'},
                            {text: 'Integration Tests', link: '/technical-details/integration-tests'},
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
