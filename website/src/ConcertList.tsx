import { FC, useEffect, useMemo, useState } from 'react';
import concertsJson from './concerts.json';
import { Concert, initialGenreFilters } from './types.ts';
import { ConcertsForDate } from './ConcertsForDate.tsx';
import { GenreFilter } from './GenreFilter.tsx';

export const ConcertList: FC = () => {
    const [concerts, setConcerts] = useState<Concert[]>([]);
    const [filters, setFilters] = useState(initialGenreFilters);

    useEffect(() => {
        setConcerts(concertsJson as Concert[]);
        // fetch('URL_TO_YOUR_JSON_FILE')
        //     .then(response => response.json())
        //     .then(data => setConcerts(data));
    }, []);

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
        <div>
            <div className='flex flex-col items-center'>
                <a className='link' href='https://t.me/MunichMetalConcerts'>
                    join the telegram METAL channel to get the newest updates
                </a>
                <a className='link' href='https://t.me/MunichRockConcerts'>
                    join the telegram ROCK channel to get the newest updates
                </a>
                <a className='link mb-4' href='https://t.me/MunichPunkConcerts'>
                    join the telegram PUNK channel to get the newest updates
                </a>
                <GenreFilter genre='metal' genreName='Metal' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='rock' genreName='Rock' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='punk' genreName='Punk' filters={filters} setFilters={setFilters} />
                <GenreFilter genre='unknown' genreName='Unknown' filters={filters} setFilters={setFilters} />
            </div>
            <table className='table max-w-7xl'>
                <thead className='bg-black text-primary'>
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
