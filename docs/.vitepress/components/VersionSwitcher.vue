<script setup lang="ts">
import { useRouter } from "vitepress"
import { computed, ref } from 'vue'
import VPMenuLink from 'vitepress/dist/client/theme-default/components/VPMenuLink.vue'
import VPFlyout from 'vitepress/dist/client/theme-default/components/VPFlyout.vue'
const props = defineProps<{
    versioningPlugin: { versions: string[], latestVersion: string }
    screenMenu?: boolean
}>();

const router = useRouter();

const sortedVersions = computed(() => {
 let versions = props.versioningPlugin.versions.toReversed();
    versions.push(props.versioningPlugin.latestVersion)
    versions.sort((a: string, b: string) => {
        // Extract the numeric parts after 'v' and compare them
        const numA = parseInt(a.slice(1), 10);
        const numB = parseInt(b.slice(1), 10);
        // Sort in descending order
        return numB - numA;
    })

    return versions
});

/**
 * generates the correct link in the dropdown for the specific version
 * @param version - the version text of the current dropdown entry
 */
function versionedLink(version: string): string {
    let pathElementsToRemove = 2
    if (!currentVersion.value.includes('latest')) {
        pathElementsToRemove = 3
    }
    const sitePath = router.route.path.split('/').slice(pathElementsToRemove).join('/')

    let returnPath = '/'
    if (!version.includes('latest')) {
        returnPath = `/${version}/`
    }

    return `${returnPath}${sitePath}`
}

const currentVersion = computed(() => {
    let version = props.versioningPlugin.latestVersion;

    for (const v of props.versioningPlugin.versions) {
        if (router.route.path.split('/').includes(v)) {
            version = v;
            break;
        }
    }

    return version;
});

const isOpen = ref(false);
const toggle = () => {
    isOpen.value = !isOpen.value;
};
</script>

<template>
    <VPFlyout v-if="!screenMenu" class="VPVersionSwitcher" icon="vpi-versioning" :button="currentVersion"
              :label="'Switch Version'">
        <div class="items">
            <template v-for="version in sortedVersions" :key="version">
                <VPMenuLink v-if="currentVersion != version" :item="{
          text: version,
          link: versionedLink(version)
        }" />
            </template>
        </div>
    </VPFlyout>
    <div v-else class="VPScreenVersionSwitcher" :class="{ open: isOpen }">
        <button class="button" aria-controls="navbar-group-version" :aria-expanded="isOpen" @click="toggle">
            <span class="button-text"><span class="vpi-versioning icon" />Switch Version</span>
            <span class="vpi-plus button-icon" />
        </button>

        <div id="navbar-group-version" class="items">
            <template v-for="version in sortedVersions" :key="version">
                <VPMenuLink :item="{
          text: version,
          link: versionedLink(version)
        }" />
            </template>
        </div>
    </div>
</template>

<style>
.vpi-versioning.option-icon {
    margin-right: 2px !important;
}

.vpi-versioning {
    --icon: url("data:image/svg+xml;charset=utf-8;base64,PHN2ZyB3aWR0aD0iNjRweCIgaGVpZ2h0PSI2NHB4IiB2aWV3Qm94PSIwIDAgMjQgMjQiIHN0cm9rZS13aWR0aD0iMi4yIiBmaWxsPSJub25lIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGNvbG9yPSIjMDAwMDAwIj48cGF0aCBkPSJNMTcgN0MxOC4xMDQ2IDcgMTkgNi4xMDQ1NyAxOSA1QzE5IDMuODk1NDMgMTguMTA0NiAzIDE3IDNDMTUuODk1NCAzIDE1IDMuODk1NDMgMTUgNUMxNSA2LjEwNDU3IDE1Ljg5NTQgNyAxNyA3WiIgc3Ryb2tlPSIjMDAwMDAwIiBzdHJva2Utd2lkdGg9IjIuMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48L3BhdGg+PHBhdGggZD0iTTcgN0M4LjEwNDU3IDcgOSA2LjEwNDU3IDkgNUM5IDMuODk1NDMgOC4xMDQ1NyAzIDcgM0M1Ljg5NTQzIDMgNSAzLjg5NTQzIDUgNUM1IDYuMTA0NTcgNS44OTU0MyA3IDcgN1oiIHN0cm9rZT0iIzAwMDAwMCIgc3Ryb2tlLXdpZHRoPSIyLjIiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCI+PC9wYXRoPjxwYXRoIGQ9Ik03IDIxQzguMTA0NTcgMjEgOSAyMC4xMDQ2IDkgMTlDOSAxNy44OTU0IDguMTA0NTcgMTcgNyAxN0M1Ljg5NTQzIDE3IDUgMTcuODk1NCA1IDE5QzUgMjAuMTA0NiA1Ljg5NTQzIDIxIDcgMjFaIiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMi4yIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjwvcGF0aD48cGF0aCBkPSJNNyA3VjE3IiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMi4yIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjwvcGF0aD48cGF0aCBkPSJNMTcgN1Y4QzE3IDEwLjUgMTUgMTEgMTUgMTFMOSAxM0M5IDEzIDcgMTMuNSA3IDE2VjE3IiBzdHJva2U9IiMwMDAwMDAiIHN0cm9rZS13aWR0aD0iMi4yIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiPjwvcGF0aD48L3N2Zz4=")
}
</style>

<style scoped>
.VPVersionSwitcher {
    display: flex;
    align-items: center;
}

.icon {
    padding: 8px;
}

.title {
    padding: 0 24px 0 12px;
    line-height: 32px;
    font-size: 14px;
    font-weight: 700;
    color: var(--vp-c-text-1);
}

.VPScreenVersionSwitcher {
    border-bottom: 1px solid var(--vp-c-divider);
    height: 48px;
    overflow: hidden;
    transition: border-color 0.5s;
}

.VPScreenVersionSwitcher .items {
    visibility: hidden;
}

.VPScreenVersionSwitcher.open .items {
    visibility: visible;
}

.VPScreenVersionSwitcher.open {
    padding-bottom: 10px;
    height: auto;
}

.VPScreenVersionSwitcher.open .button {
    padding-bottom: 6px;
    color: var(--vp-c-brand-1);
}

.VPScreenVersionSwitcher.open .button-icon {
    /*rtl:ignore*/
    transform: rotate(45deg);
}

.VPScreenVersionSwitcher button .icon {
    margin-right: 8px;
}

.button {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 4px 11px 0;
    width: 100%;
    line-height: 24px;
    font-size: 14px;
    font-weight: 500;
    color: var(--vp-c-text-1);
    transition: color 0.25s;
}

.button:hover {
    color: var(--vp-c-brand-1);
}

.button-icon {
    transition: transform 0.25s;
}

.group:first-child {
    padding-top: 0px;
}

.group+.group,
.group+.item {
    padding-top: 4px;
}
</style>