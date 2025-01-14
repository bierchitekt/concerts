import { Concert, Genre, GenreFilters, initialGenreFilters } from './types.ts';

export function matchesGenre(concert: Concert, filters: GenreFilters) {
    if (Object.values(filters).every((filter) => !filter)) {
        return true;
    }

    const cleanedGenres = cleanGenres(concert.genre);
    return cleanedGenres.some((genre) => filters[genre]);
}

export function cleanGenres(genres: string[]): Genre[] {
    const knownGenres = Object.keys(initialGenreFilters).filter((it) => it !== 'unknown') as Genre[];

    const cleanedGenres = new Set<Genre>();
    for (const genre of genres) {
        const matchingGenre = knownGenres.find((knownGenre) => genre.toLowerCase().includes(knownGenre.toLowerCase()));
        if (matchingGenre) {
            cleanedGenres.add(matchingGenre);
        }
    }

    if (cleanedGenres.size === 0) {
        return ['unknown'];
    }

    return [...cleanedGenres];
}
