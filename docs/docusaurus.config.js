// @ts-check

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Kindling',
  tagline: 'Tools for Ignition Power Users',
  favicon: 'img/favicon.ico',

  url: 'https://inductiveautomation.github.io',
  baseUrl: '/kindling/',

  organizationName: 'inductiveautomation',
  projectName: 'kindling',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          editUrl: 'https://github.com/inductiveautomation/kindling/tree/main/docs/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Kindling',
        logo: {
          alt: 'Kindling Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'tutorialSidebar',
            position: 'left',
            label: 'Documentation',
          },
          {
            href: 'https://inductiveautomation.github.io/kindling/download.html',
            label: 'Download',
            position: 'right',
          },
          {
            href: 'https://github.com/inductiveautomation/kindling',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Getting Started',
                to: '/docs/intro',
              },
              {
                label: 'Tools',
                to: '/docs/tools/overview',
              },
              {
                label: 'CLI',
                to: '/docs/cli/overview',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'IA Forum',
                href: 'https://forum.inductiveautomation.com/',
              },
              {
                label: 'GitHub Issues',
                href: 'https://github.com/inductiveautomation/kindling/issues',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/inductiveautomation/kindling',
              },
              {
                label: 'Download',
                href: 'https://inductiveautomation.github.io/kindling/download.html',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Inductive Automation. Built with Docusaurus.`,
      },
      prism: {
        theme: require('prism-react-renderer').themes.github,
        darkTheme: require('prism-react-renderer').themes.dracula,
        additionalLanguages: ['kotlin', 'bash', 'json'],
      },
    }),
};

module.exports = config;
