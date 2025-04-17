import { Concert } from './types.ts';
import ConcertItem from './ConcertItem.tsx';
import { FC } from 'react';

type ConcertsForDateProps = {
    date: string;
    concerts: Concert[];
};

export const ConcertsForDate: FC<ConcertsForDateProps> = ({ date, concerts }) => {
    if (concerts.length === 0) {
        return null;
    }

    return (
        <div>
            <div className='bg-base-100 text-primary sticky top-0 z-10 p-4 font-bold'>{date}</div>
            {concerts.map((concert, index) => (
                <ConcertItem key={index} concert={concert} />
            ))}
        </div>
    );
};
