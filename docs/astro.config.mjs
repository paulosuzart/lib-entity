// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
			title: 'LibEntity',
			social: {
				github: 'https://github.com/yourusername/lib-entity',
			},
			sidebar: [
				{
					label: 'Getting Started',
					items: [
						{ label: 'Introduction', link: '/' },
						{ label: 'Installation', link: '/guides/installation/' },
						{ label: 'Quick Start', link: '/guides/quick-start/' },
					],
				},
				{
					label: 'Core Concepts',
					items: [
						{ label: 'Entities', link: '/concepts/entities/' },
						{ label: 'States', link: '/concepts/states/' },
						{ label: 'Actions', link: '/concepts/actions/' },
						{ label: 'Fields', link: '/concepts/fields/' },
						{ label: 'Validation', link: '/concepts/validation/' },
						{ label: 'Response Types', link: '/concepts/response-types/' },
						{ label: 'EntityStore', link: '/docs/concepts/entity-store/' },
					],
				},
				// {
				// 	label: 'Advanced Usage',
				// 	items: [
				// 		{ label: 'State Transitions', link: '/advanced/state-transitions/' },
				// 		{ label: 'Conditional Actions', link: '/advanced/conditional-actions/' },
				// 		{ label: 'Spring Boot Integration', link: '/advanced/spring-boot/' },
				// 	],
				// },
				// {
				// 	label: 'API Reference',
				// 	items: [
				// 		{ label: 'EntityType', link: '/reference/entity-type/' },
				// 		{ label: 'Action', link: '/reference/action/' },
				// 		{ label: 'Field', link: '/reference/field/' },
				// 		{ label: 'EntityContext', link: '/reference/entity-context/' },
				// 	],
				// },
			],
		}),
	],
});
