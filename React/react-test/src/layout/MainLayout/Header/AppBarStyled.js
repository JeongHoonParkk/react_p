import { AppBar, Toolbar } from "@mui/material";
import Timer from "../../../components/CurrentTime/currentTime";

const AppBarStyled = () => {
  return (
    <AppBar
      position="static"
      sx={{
        bgcolor: "white",
        color: "#454C53",
        fontSize: "2.5em",
        fontWeight: "bold",
      }}
    >
      <Toolbar>
        <Timer />
      </Toolbar>
    </AppBar>
  );
};

export default AppBarStyled;
