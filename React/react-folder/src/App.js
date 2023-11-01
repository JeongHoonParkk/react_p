const createScream = logger => message => {
  logger(message.toUppercase())
}

const scream = createScream(message => console.log(message));


scream('함수가 함수 반환')
scream('createScream은 함수 반환')
scream('scream 은 createScream이 반환한 함수를 가리킴')



function App() {
  return (
    <div className="App">
      <header className="App-header">
        
      </header>
    </div>
  );
}

export default App;
