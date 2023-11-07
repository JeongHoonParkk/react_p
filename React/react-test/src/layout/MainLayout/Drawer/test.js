import * as React from "react";
import Box from "@mui/material/Box";
import Switch from "@mui/material/Switch";
import Paper from "@mui/material/Paper";
import Collapse from "@mui/material/Collapse";
import FormControlLabel from "@mui/material/FormControlLabel";

const icon = (
  <Paper sx={{ m: 1, width: 100, height: 100 }} elevation={4}>
    <svg>
      <Box
        component="polygon"
        points="0,100 50,00, 100,100"
        sx={{
          fill: (theme) => theme.palette.common.white,
          stroke: (theme) => theme.palette.divider,
          strokeWidth: 1,
        }}
      />
    </svg>
  </Paper>
);

export default function SimpleCollapse() {
  const [checked, setChecked] = React.useState(false);

  const handleChange = () => {
    setChecked((prev) => !prev);
  };

  return (
    <Box sx={{ height: 300 }}>
      <FormControlLabel
        control={<Switch checked={checked} onChange={handleChange} />}
        label="Show"
      />
      <Box
        sx={{
          "& > :not(style)": {
            display: "flex",
            justifyContent: "space-around",
            height: 10,
            width: 250,
          },
        }}
      >
        <div style={{ border: "1px solid black" }}>
          <Box sx={{ width: "100%" }}>
            <Collapse orientation="horizontal" in={checked}>
              {icon}
            </Collapse>
          </Box>
        </div>
      </Box>
    </Box>
  );
}
