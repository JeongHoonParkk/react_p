import AppBarStyled from "./layout/MainLayout/Header/AppBarStyled";
import Sheet from "@mui/joy/Sheet";
import PrimarySearchAppBar from "./components/notification";

const App = () => {
  return (
    <>
      <PrimarySearchAppBar></PrimarySearchAppBar>
      <AppBarStyled></AppBarStyled>
      <Sheet color="primary" variant="soft" sx={{ p: 100 }}></Sheet>
    </>
  );
};
export default App;
