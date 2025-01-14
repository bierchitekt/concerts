import { Concert, GenreFilters } from './types.ts';
import ConcertItem from './ConcertItem.tsx';
import { FC, useMemo } from 'react';
import { matchesGenre } from './matchesGenre.ts';

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
        <div>
            <div className='sticky top-0 z-10 bg-base-100 p-4 font-bold text-primary'>{date}</div>
            {filteredConcerts.map((concert, index) => (
                <ConcertItem key={index} concert={concert} />
            ))}
        </div>
    );
};
