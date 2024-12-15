export type Concert = {
    title: string;
    date: [number, number, number];
    link: string;
    genre: string[];
    location: string;
    supportBands: string;
};

export const initialGenreFilters = { metal: true, rock: true, punk: true, unknown: true };

export type GenreFilters = typeof initialGenreFilters;
export type Genre = keyof GenreFilters;
