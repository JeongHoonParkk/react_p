import React, { useState } from "react";

export default function Timer() {
  const [timer, setTimer] = useState("00:00:00");
  const [formDate, setFormDate] = useState("0000-00-00");

  const currentTimer = () => {
    const date = new Date();
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const seconds = String(date.getSeconds()).padStart(2, "0");
    const year = String(date.getFullYear());
    const month = String(date.getMonth() + 1);
    const day = String(date.getDate());
    setTimer(`${hours}:${minutes}:${seconds}`);
    setFormDate(`${year}-${month}-${day}`);
  };

  const startTimer = () => {
    setInterval(currentTimer, 1000);
  };

  startTimer();
  return (
    <>
      {formDate} {timer}
    </>
  );
}
