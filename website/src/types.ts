export type Concert = {
    title: string;
    date: [number, number, number];
    link: string;
    genre: string[];
    location: string;
    supportBands: string;
};

export const initialGenreFilters = { metal: false, rock: false, punk: false, unknown: false };

export type GenreFilters = typeof initialGenreFilters;
export type Genre = keyof GenreFilters;
