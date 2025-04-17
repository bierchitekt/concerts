import { Dispatch, FC, PropsWithChildren, SetStateAction, useEffect, useMemo, useState } from 'react';
import { Concert, initialGenreFilters } from './types.ts';
import { ConcertsForDate } from './ConcertsForDate.tsx';
import { GenreFilter } from './GenreFilter.tsx';
import { ConcertFilter, GenreConcertFilter, TextConcertFilter } from './ConcertFilter.ts';

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
    const [concertsFilter, setConcertsFilter] = useState<ConcertFilter>({ filter: () => true });

    const concertsByDate = useMemo(() => {
        const filteredConcerts = concerts.filter((concert) => concertsFilter.filter(concert));

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
    }, [concerts, concertsFilter]);

    return (
        <>
            <ConcertFilters setConcertsFilter={setConcertsFilter} />
            <div>
                {[...concertsByDate.entries()].map(([date, concerts]) => (
                    <ConcertsForDate key={date} date={date} concerts={concerts} />
                ))}
            </div>
        </>
    );
};

type ConcertFiltersProps = {
    setConcertsFilter: Dispatch<SetStateAction<ConcertFilter>>;
};

const ConcertFilters: FC<ConcertFiltersProps> = ({ setConcertsFilter }: ConcertFiltersProps) => {
    const [genreFilters, setGenreFilters] = useState(initialGenreFilters);
    const [textFilter, setTextFilter] = useState('');

    useEffect(() => {
        setConcertsFilter(
            new GenreConcertFilter(genreFilters).combine(new TextConcertFilter(textFilter.toLowerCase())),
        );
    }, [genreFilters, textFilter, setConcertsFilter]);

    return (
        <div className='my-2'>
            <p className='text-center text-xl font-bold text-primary'>Filter concerts</p>
            <div className='flex flex-wrap justify-center'>
                <FilterGroupContainer>
                    <p className='font-bold'>By genre:</p>
                    <GenreFilter genre='metal' filters={genreFilters} setFilters={setGenreFilters} />
                    <GenreFilter genre='rock' filters={genreFilters} setFilters={setGenreFilters} />
                    <GenreFilter genre='punk' filters={genreFilters} setFilters={setGenreFilters} />
                    <GenreFilter genre='unknown' filters={genreFilters} setFilters={setGenreFilters} />
                </FilterGroupContainer>
                <FilterGroupContainer>
                    <p className='font-bold'>By text:</p>
                    <label className='input-bordered input my-2 flex items-center gap-2'>
                        <span className='iconify text-xl mdi--magnify' />
                        <input
                            placeholder='Search...'
                            className='flex-grow'
                            value={textFilter}
                            onChange={(e) => setTextFilter(e.target.value)}
                        />
                        <ResetButton hidden={textFilter.length == 0} onClick={() => setTextFilter('')} />
                    </label>
                </FilterGroupContainer>
            </div>
        </div>
    );
};

const FilterGroupContainer: FC<PropsWithChildren> = ({ children }) => (
    <div className='bottom-1 m-1 min-w-60 max-w-80 flex-grow'>{children}</div>
);

type ResetButtonProps = { hidden: boolean; onClick: () => void };

const ResetButton: FC<ResetButtonProps> = ({ hidden, onClick }) => {
    return (
        <button className={`z-10 text-xl ${hidden ? 'hidden' : ''}`} onClick={onClick}>
            <svg
                xmlns='http://www.w3.org/2000/svg'
                className='h-6 w-6'
                fill='none'
                viewBox='0 0 24 24'
                stroke='currentColor'
            >
                <path strokeLinecap='round' strokeLinejoin='round' strokeWidth='2' d='M6 18L18 6M6 6l12 12' />
            </svg>
        </button>
    );
};
