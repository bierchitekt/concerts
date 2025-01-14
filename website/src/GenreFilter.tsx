import { GenreFilters, iconMap } from './types.ts';
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
            <div className='mx-2 flex items-center'>
                <span className={`iconify ${iconMap[genre]} mr-1`} />
                {genreName}
            </div>
            <input
                className='toggle toggle-primary'
                type='checkbox'
                checked={filters[genre]}
                onChange={() => setFilters((prevFilters) => ({ ...prevFilters, [genre]: !prevFilters[genre] }))}
            />
        </label>
    );
};
