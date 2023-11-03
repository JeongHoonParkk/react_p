import React, { useEffect, useState } from "react";
import Timer from "./components/Timer";

const App = () => {
  const [showTimer, setShowTimer] = useState(false);
  useEffect(() => {
    console.log("앱 렌더됨");
  }, []);
  return (
    <div>
      {showTimer && <Timer />}
      <button onClick={() => setShowTimer(!showTimer)}>Toggle Timer</button>
    </div>
  );
};

export default App;
