import { Concert, Genre, GenreFilters, initialGenreFilters } from './types.ts';
import ConcertItem from './ConcertItem.tsx';
import { FC, useMemo } from 'react';

type ConcertsForDateProps = {
    date: string;
    concerts: Concert[];
    filters: GenreFilters;
};

export const ConcertsForDate: FC<ConcertsForDateProps> = ({ date, concerts, filters }) => {
    const filteredConcerts = useMemo(
        () => concerts.filter((concert) => matchesGenre(concert, filters)),
        [concerts, filters],
    );

    if (filteredConcerts.length === 0) {
        return null;
    }

    return (
        <>
            <tr className='bg-base-200 font-bold'>
                <td colSpan={4}>{date}</td>
            </tr>
            {filteredConcerts.map((concert, index) => (
                <ConcertItem key={index} concert={concert} />
            ))}
        </>
    );
};

function matchesGenre(concert: Concert, filters: GenreFilters) {
    if (Object.values(filters).every((filter) => !filter)) {
        return true;
    }

    const cleanedGenres = cleanGenres(concert.genre);
    return cleanedGenres.some((genre) => filters[genre]);
}

function cleanGenres(genres: string[]): Genre[] {
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
