import React from "react";
import { useEffect } from "react";

const Timer = (props) => {
  console.log("1111");

  useEffect(() => {
    console.log("타이머 렌더됨");
    const timer = setInterval(() => {
      // 타이머 컴포턴트가 화면에 렌더링 됐을 때
      console.log("타이머 돌아가는중 ...");
    }, 1000);

    return () => {
      // 타이머 컴포넌트가 Unmount 될 때 실행됨 (타이머가 끝나도록 도움)
      clearInterval(timer);
      console.log("타이머 종료");
    };
  }, []);

  return (
    <div>
      <span>타이머를 시작합니다. 콘솔을 보세요</span>
    </div>
  );
};

export default Timer;
