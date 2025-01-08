import DefaultTheme from 'vitepress/theme-without-fonts'
import './custom.css'
import { Theme, useData, useRoute } from 'vitepress';
import VersionSwitcher from "../components/VersionSwitcher.vue";
import codeblocksFold from 'vitepress-plugin-codeblocks-fold'; // import method
import 'vitepress-plugin-codeblocks-fold/style/index.css'; // import style

export default {
    extends: DefaultTheme,
    enhanceApp({ app }) {
        app.component('VersionSwitcher', VersionSwitcher)
    },
    setup() {
        const { frontmatter } = useData();
        const route = useRoute();
        codeblocksFold({ route, frontmatter }, false, 200);
    }
} satisfies Theme;