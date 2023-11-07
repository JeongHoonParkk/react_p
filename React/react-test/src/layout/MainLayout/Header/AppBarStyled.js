import { AppBar, Toolbar } from "@mui/material";
import Timer from "../../../components/CurrentTime/currentTime";
import PrimarySearchAppBar from "../../../components/notification";
import AccountMenu from "./HeaderContent/profile/profileTab";

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
        <PrimarySearchAppBar></PrimarySearchAppBar>
        <AccountMenu></AccountMenu>
      </Toolbar>
    </AppBar>
  );
};

export default AppBarStyled;
