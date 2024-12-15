import { FC } from 'react';
import { Concert } from './types.ts';

type ConcertItemProps = {
    concert: Concert;
};

const ConcertItem: FC<ConcertItemProps> = ({ concert }) => {
    const { title, link, genre, location, supportBands } = concert;

    return (
        <tr className='hover'>
            <td>
                <a className='link' href={link} target='_blank' rel='noopener noreferrer'>
                    {title}
                </a>
            </td>
            <td>{genre.join(', ')}</td>
            <td>{supportBands}</td>
            <td>{location}</td>
        </tr>
    );
};

export default ConcertItem;
