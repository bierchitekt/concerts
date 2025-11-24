import { FC } from 'react';
import { Concert, iconMap } from './types.ts';
import { cleanGenres } from './matchesGenre.ts';

type ConcertItemProps = {
    concert: Concert;
};

const ConcertItem: FC<ConcertItemProps> = ({ concert }) => {
    const { title, link, genre, location, supportBands, price, calendarUri } = concert;

    return (
        <div className='card-compact card bg-base-300 my-2 shadow-sm'>
            <div className='card-body'>
                <div className='flex w-full items-center font-bold'>
                    <div className='grow'>
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
                <div>
                    <p>{price ? `${price}` : ''}</p>
                </div>
                <div className='grid grid-cols-3 gap-4'>
                    <p>{genre.join(', ')}</p>
                    <p>{supportBands}</p>
                    <p className='text-end'>{location}</p>
                    <a className='link' href={calendarUri} target='_blank' rel='noopener noreferrer'>
                        add to calendar
                    </a>
                </div>
            </div>
        </div>
    );
};

export default ConcertItem;
