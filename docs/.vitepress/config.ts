import { defineConfig, DefaultTheme } from 'vitepress'

export default defineConfig({
  title: 'Abloom-API',
  description: 'Элементальная система урона с резонансным накоплением для NeoForge 1.21.1',
  lang: 'en-US',
  base: '/abloom-api-docs/',
  cleanUrls: true,

  themeConfig: {
    logo: '/logo.svg',
    search: {
      provider: 'local'
    },
    nav: [
      { text: 'Docs', link: '/getting-started', activeMatch: '/docs/' },
      { text: 'API', link: '/api-reference' },
      { text: 'Datapack', link: '/datapack-guide' },
      {
        text: 'v1.0.0-beta.19',
        items: [
          { text: 'Changelog', link: 'https://github.com/AuraniteStudio/Abloom-API/releases' },
          { text: 'GitHub', link: 'https://github.com/AuraniteStudio/Abloom-API' }
        ]
      }
    ],

    sidebar: {
      '/docs/': [
        {
          text: 'Documentation',
          items: [
            { text: 'Getting Started', link: '/getting-started' },
            { text: 'Core Concepts', link: '/core-concepts' },
            { text: 'Effects Reference', link: '/effects' },
            { text: 'Configuration', link: '/config' }
          ]
        },
        {
          text: 'Integration',
          items: [
            { text: 'API Reference', link: '/api-reference' },
            { text: 'Datapack Guide', link: '/datapack-guide' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/AuraniteStudio/Abloom-API' }
    ],

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2026 Auranite Studio'
    },

    editLink: {
      pattern: 'https://github.com/AuraniteStudio/Abloom-API/edit/main/docs/:path',
      text: 'Edit this page on GitHub'
    }
  },

  head: [
    ['link', { rel: 'icon', type: 'image/svg+xml', href: '/logo.svg' }],
    ['meta', { name: 'theme-color', content: '#5F3DCB' }],
    ['meta', { property: 'og:title', content: 'Abloom-API Documentation' }],
    ['meta', { property: 'og:description', content: 'Elemental damage system with resonance accumulation for NeoForge 1.21.1' }]
  ]
})
