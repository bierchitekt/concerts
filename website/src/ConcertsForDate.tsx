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
            <div className='sticky top-0 z-10 bg-base-100 p-4 font-bold text-primary'>{date}</div>
            {concerts.map((concert, index) => (
                <ConcertItem key={index} concert={concert} />
            ))}
        </div>
    );
};
