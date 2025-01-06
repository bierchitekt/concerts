import { FC, useEffect, useMemo, useState } from 'react';
import { Concert, initialGenreFilters } from './types.ts';
import { ConcertsForDate } from './ConcertsForDate.tsx';
import { GenreFilter } from './GenreFilter.tsx';
import { matchesGenre } from './matchesGenre.ts';

export const ConcertList: FC = () => {
    const [concerts, setConcerts] = useState<Concert[] | Error | undefined>(undefined);

    useEffect(() => {
        (async () => {
            try {
                const response = await fetch(import.meta.env.VITE_CONCERTS_JSON_URL);

                if (!response.ok) {
                    if (response.status === 404) {
                        setConcerts(new Error('Concerts not found'));
                        return;
                    }

                    setConcerts(new Error(await response.text()));
                    return;
                }

                setConcerts(await response.json());
            } catch (error) {
                setConcerts(error as Error);
            }
        })();
    }, []);

    if (concerts === undefined) {
        return <span className='loading loading-spinner' />;
    }

    if (concerts instanceof Error) {
        console.error(concerts);
        return <span className='alert alert-error'>{concerts.message}</span>;
    }

    return <ConcertListInner concerts={concerts} />;
};

const ConcertListInner: FC<{ concerts: Concert[] }> = ({ concerts }) => {
    const [filters, setFilters] = useState(initialGenreFilters);

    const concertsByDate = useMemo(() => {
        const filteredConcerts = concerts.filter((concert) => matchesGenre(concert, filters));

        const concertsByDate = new Map<string, Concert[]>();

        for (const concert of filteredConcerts) {
            const [year, month, day] = concert.date;
            const dateString = new Date(year, month - 1, day).toLocaleDateString('en-us', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
            });
            if (!concertsByDate.has(dateString)) {
                concertsByDate.set(dateString, []);
            }

            concertsByDate.get(dateString)!.push(concert);
        }

        return concertsByDate;
    }, [concerts, filters]);

    return (
        <>
            <div className='flex flex-col items-center'>
                <p className='font-bold'>Filter by genre:</p>
                <GenreFilter genre='metal' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='rock' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='punk' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='unknown' filters={filters} setFilters={setFilters} />
            </div>
            <div>
                {[...concertsByDate.entries()].map(([date, concerts]) => (
                    <ConcertsForDate key={date} date={date} concerts={concerts} />
                ))}
            </div>
        </>
    );
};
