import {useEffect, useState} from "react";
import concertsJson from "./concerts.json";
import ConcertItem from "./ConcertItem.tsx";

export function ConcertList() {

    const [concerts, setConcerts] = useState([]);
    const [filters, setFilters] = useState({metal: false, rock: false, punk: false});

    useEffect(() => {
        setConcerts(concertsJson);
        // fetch('URL_TO_YOUR_JSON_FILE')
        //     .then(response => response.json())
        //     .then(data => setConcerts(data));
    }, []);

    const handleFilterChange = (genre) => {
        setFilters(prevFilters => ({
            ...prevFilters,
            [genre]: !prevFilters[genre]
        }));
    };

    const filteredConcerts = concerts.filter(concert => {
        if (filters.metal && concert.genre.includes('Metal')) return true;
        if (filters.rock && concert.genre.includes('Rock')) return true;
        if (filters.punk && concert.genre.includes('Punk')) return true;
        return !filters.metal && !filters.rock && !filters.punk;
    });

    return (
        <div>
            <div>
                <label>
                    <input type="checkbox" checked={filters.metal} onChange={() => handleFilterChange('metal')}/>
                    Metal
                </label>
                <label>
                    <input type="checkbox" checked={filters.rock} onChange={() => handleFilterChange('rock')}/>
                    Rock
                </label>
                <label>
                    <input type="checkbox" checked={filters.punk} onChange={() => handleFilterChange('punk')}/>
                    Punk
                </label>
            </div>
            <table>
                <thead>
                <tr>
                    <th>Band/Date</th>
                    <th>Genre</th>
                    <th>Support</th>
                    <th>Location</th>
                </tr>
                </thead>
                <tbody>
                {filteredConcerts.map((concert, index) => (
                    <ConcertItem key={index} concert={concert}/>
                ))}
                </tbody>
            </table>
        </div>
    );
}
