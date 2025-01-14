import { FC } from 'react';
import { Concert, iconMap } from './types.ts';
import { cleanGenres } from './matchesGenre.ts';

type ConcertItemProps = {
    concert: Concert;
};

const ConcertItem: FC<ConcertItemProps> = ({ concert }) => {
    const { title, link, genre, location, supportBands } = concert;

    return (
        <div className='card card-compact my-2 bg-base-300 shadow'>
            <div className='card-body'>
                <div className='flex w-full items-center font-bold'>
                    <div className='flex-grow'>
                        <a className='link' href={link} target='_blank' rel='noopener noreferrer'>
                            {title}
                        </a>
                    </div>
                    {cleanGenres(genre)
                        .filter((it) => it !== 'unknown')
                        .map((it) => (
                            <span key={it} className={`iconify ${iconMap[it]} mr-1 text-xl`} />
                        ))}
                </div>
                <div className='grid grid-cols-3 gap-4'>
                    <p>{genre.join(', ')}</p>
                    <p>{supportBands}</p>
                    <p className='text-end'>{location}</p>
                </div>
            </div>
        </div>
    );
};

export default ConcertItem;
