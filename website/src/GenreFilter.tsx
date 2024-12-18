import { GenreFilters } from './types.ts';
import { Dispatch, FC, SetStateAction } from 'react';

type GenreFilterProps = {
    genre: keyof GenreFilters;
    genreName: string;
    filters: GenreFilters;
    setFilters: Dispatch<SetStateAction<GenreFilters>>;
};

export const GenreFilter: FC<GenreFilterProps> = ({ genre, genreName, filters, setFilters }) => {
    return (
        <label className='label cursor-pointer py-2'>
            <span className='mx-2'>{genreName}</span>
            <input
                className='toggle toggle-primary'
                type='checkbox'
                checked={filters[genre]}
                onChange={() => setFilters((prevFilters) => ({ ...prevFilters, [genre]: !prevFilters[genre] }))}
            />
        </label>
    );
};
