/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  tutorialSidebar: [
    'intro',
    {
      type: 'category',
      label: 'Tools',
      items: [
        'tools/overview',
        'tools/thread-viewer',
        'tools/idb-viewer',
        'tools/log-viewer',
        'tools/archive-explorer',
        'tools/cache-viewer',
        'tools/directory-viewer',
        'tools/git-branch-viewer',
      ],
    },
    {
      type: 'category',
      label: 'CLI Mode',
      items: [
        'cli/overview',
        'cli/analyze-directory',
        'cli/compare-branches',
        'cli/analyze',
        'cli/backup-stats',
      ],
    },
    {
      type: 'category',
      label: 'Development',
      items: [
        'development/getting-started',
        'development/architecture',
        'development/adding-tools',
        'development/contributing',
      ],
    },
  ],
};

module.exports = sidebars;
