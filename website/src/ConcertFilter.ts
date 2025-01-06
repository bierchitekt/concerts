import { Concert, GenreFilters } from './types.ts';
import { matchesGenre } from './matchesGenre.ts';

export interface ConcertFilter {
    filter(concert: Concert): boolean;
}

export abstract class BaseConcertFilter implements ConcertFilter {
    abstract filter(concert: Concert): boolean;

    public combine(other: ConcertFilter): ConcertFilter {
        return {
            filter: (concert) => this.filter(concert) && other.filter(concert),
        };
    }
}

export class GenreConcertFilter extends BaseConcertFilter {
    constructor(private readonly genreFilters: GenreFilters) {
        super();
    }

    public filter(concert: Concert): boolean {
        return matchesGenre(concert, this.genreFilters);
    }
}

export class TextConcertFilter extends BaseConcertFilter {
    constructor(private readonly searchString: string) {
        super();
    }

    public filter(concert: Concert): boolean {
        if (this.searchString.length < 1) {
            return true;
        }

        const { date, ...propertiesToFilter } = concert;

        return Object.values(propertiesToFilter).some((value) => {
            if (Array.isArray(value)) {
                return value.some((it) => it.toLowerCase().includes(this.searchString));
            }
            return value.toLowerCase().includes(this.searchString);
        });
    }
}
