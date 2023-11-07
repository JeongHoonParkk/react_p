import Sheet from "@mui/joy/Sheet";
import AppBarStyled from "./layout/MainLayout/Header/AppBarStyled";

const App = () => {
  return (
    <>
      <AppBarStyled />
      <Sheet color="primary" variant="soft" sx={{ p: 100 }}></Sheet>
    </>
  );
};
export default App;
