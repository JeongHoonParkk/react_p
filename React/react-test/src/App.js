import Test from './components/test';
import './App.css';
import Variants from './components/link';



const App = () => {
  return (
    <div className='App'>
      <Test/>
      <div className='black-nav'>
        <h1>css test</h1>
        <div className='Vari'>
          <Variants></Variants>
        </div>
      </div>
    </div>
  );
}

export default App;
