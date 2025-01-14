import daisyui from 'daisyui';
import { addIconSelectors } from '@iconify/tailwind';

/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
    theme: {
        extend: {},
    },
    plugins: [daisyui, addIconSelectors(['mdi', 'fluent-emoji-high-contrast', 'emojione-monotone', 'game-icons'])],
    daisyui: {
        themes: ['light', 'dark'],
    },
};
