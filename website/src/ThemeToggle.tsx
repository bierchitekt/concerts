import { FC } from 'react';

export const ThemeToggle: FC = () => {
    const nonDefaultTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'light' : 'dark';

    const sunIconSwap = nonDefaultTheme === 'light' ? 'swap-on' : 'swap-off';
    const moonIconSwap = nonDefaultTheme === 'light' ? 'swap-off' : 'swap-on';

    return (
        <label className='swap swap-rotate'>
            <input type='checkbox' className='theme-controller' value={nonDefaultTheme} />
            <svg
                className={`${sunIconSwap} size-6 fill-current`}
                xmlns='http://www.w3.org/2000/svg'
                viewBox='0 0 24 24'
            >
                <path d='M5.64,17l-.71.71a1,1,0,0,0,0,1.41,1,1,0,0,0,1.41,0l.71-.71A1,1,0,0,0,5.64,17ZM5,12a1,1,0,0,0-1-1H3a1,1,0,0,0,0,2H4A1,1,0,0,0,5,12Zm7-7a1,1,0,0,0,1-1V3a1,1,0,0,0-2,0V4A1,1,0,0,0,12,5ZM5.64,7.05a1,1,0,0,0,.7.29,1,1,0,0,0,.71-.29,1,1,0,0,0,0-1.41l-.71-.71A1,1,0,0,0,4.93,6.34Zm12,.29a1,1,0,0,0,.7-.29l.71-.71a1,1,0,1,0-1.41-1.41L17,5.64a1,1,0,0,0,0,1.41A1,1,0,0,0,17.66,7.34ZM21,11H20a1,1,0,0,0,0,2h1a1,1,0,0,0,0-2Zm-9,8a1,1,0,0,0-1,1v1a1,1,0,0,0,2,0V20A1,1,0,0,0,12,19ZM18.36,17A1,1,0,0,0,17,18.36l.71.71a1,1,0,0,0,1.41,0,1,1,0,0,0,0-1.41ZM12,6.5A5.5,5.5,0,1,0,17.5,12,5.51,5.51,0,0,0,12,6.5Zm0,9A3.5,3.5,0,1,1,15.5,12,3.5,3.5,0,0,1,12,15.5Z' />
            </svg>
            <span className={`iconify text-2xl mdi--moon-and-stars ${moonIconSwap}`} />
        </label>
    );
};
