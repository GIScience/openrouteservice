import DefaultTheme from 'vitepress/theme-without-fonts'
import './custom.css'
import { Theme } from 'vitepress';
import VersionSwitcher from "../components/VersionSwitcher.vue";

export default {
    extends: DefaultTheme,
    enhanceApp({ app }) {
        app.component('VersionSwitcher', VersionSwitcher)
    }
} satisfies Theme;