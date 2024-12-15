import { ConcertList } from './ConcertList.tsx';

function App() {
    return (
        <div className='flex flex-col items-center'>
            <h1 className='m-8 text-3xl font-bold'>All Concerts in Munich</h1>
            <ConcertList />
        </div>
    );
}

export default App;
