import { FC, useEffect, useMemo, useState } from 'react';
import { Concert, initialGenreFilters } from './types.ts';
import { ConcertsForDate } from './ConcertsForDate.tsx';
import { GenreFilter } from './GenreFilter.tsx';

export const ConcertList: FC = () => {
    const [concerts, setConcerts] = useState<Concert[] | Error | undefined>(undefined);

    useEffect(() => {
        (async () => {
            try {
                const response = await fetch('https://pubrrr.github.io/concert-html/concerts.json');

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
        return <span className='alert alert-error'>{concerts.message}</span>;
    }

    return <ConcertListInner concerts={concerts} />;
};

const ConcertListInner: FC<{ concerts: Concert[] }> = ({ concerts }) => {
    const [filters, setFilters] = useState(initialGenreFilters);

    const concertsByDate = useMemo(() => {
        const concertsByDate = new Map<string, Concert[]>();

        for (const concert of concerts) {
            const [year, month, day] = concert.date;
            let dateString = new Date(year, month - 1, day).toLocaleDateString('en-us', {
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
    }, [concerts]);

    return (
        <div className='p-8'>
            <div className='flex flex-col items-center'>
                <GenreFilter genre='metal' genreName='Metal' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='rock' genreName='Rock' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='punk' genreName='Punk' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='unknown' genreName='Unknown' filters={filters} setFilters={setFilters} />
            </div>
            <table className='table max-w-[1600px]'>
                <thead className='bg-neutral text-lg font-bold text-primary'>
                    <tr>
                        <th>Band/Date</th>
                        <th>Genre</th>
                        <th>Support</th>
                        <th>Location</th>
                    </tr>
                </thead>
                <tbody>
                    {[...concertsByDate.entries()].map(([date, concerts]) => (
                        <ConcertsForDate key={date} filters={filters} date={date} concerts={concerts} />
                    ))}
                </tbody>
            </table>
        </div>
    );
};
