const ConcertItem = ({concert}) => {
    const {title, date, link, genre, location, supportBands} = concert;
    const formattedDate = new Date(date[0], date[1] - 1, date[2]).toLocaleDateString('de-DE');

    return (
        <tr>
            <td><a className="link" href={link} target="_blank" rel="noopener noreferrer">{title}</a> - {formattedDate}</td>
            <td>{genre.join(', ')}</td>
            <td>{supportBands}</td>
            <td>{location}</td>
        </tr>
    );
};

export default ConcertItem;
