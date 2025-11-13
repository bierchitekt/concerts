export type Concert = {
    title: string;
    date: string;
    link: string;
    genre: string[];
    location: string;
    supportBands: string;
    price: string;
};

export const initialGenreFilters = { metal: false, rock: false, punk: false, hardcore: false, unknown: false };

export type GenreFilters = typeof initialGenreFilters;
export type Genre = keyof GenreFilters;

export const iconMap = {
    metal: 'emojione-monotone--sign-of-the-horns',
    rock: 'emojione-monotone--guitar',
    punk: 'game-icons--anarchy',
    hardcore: 'game-icons--shouting',
    unknown: 'emojione-monotone--white-question-mark',
} satisfies Record<keyof GenreFilters, string>;
