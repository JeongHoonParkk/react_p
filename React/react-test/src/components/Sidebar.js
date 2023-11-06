import { styled } from "@mui/material/styles";

// screen width가 lg(large: 1200px) 보다 크면 width에 280 적용
const DRAWER_WIDTH = 280;
const RootStyle = styled("div")(({ theme }) => ({
  [theme.breakpoints.up("lg")]: {
    flexShrink: 0,
    width: DRAWER_WIDTH,
  },
}));

function SidebarComponent() {
  return <RootStyle>....</RootStyle>;
}

export default SidebarComponent;
