// .vitepress/theme/index.js
import DefaultTheme from 'vitepress/theme-without-fonts'
import VersionSwitcher from 'vitepress-versioning-plugin/src/components/VersionSwitcher.vue'
import './custom.css'
import { Theme } from 'vitepress';

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) { 
    app.component('VersionSwitcher', VersionSwitcher) 
  } 
} satisfies Theme;
