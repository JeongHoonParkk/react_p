import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";
import Badge from "@mui/material/Badge";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import Toolbar from "@mui/material/Toolbar";
import * as React from "react";

export default function PrimarySearchAppBar() {
  return (
    <Box sx={{ flexGrow: 1 }}>
      <Toolbar>
        <Box sx={{ flexGrow: 1 }} />
        <Box sx={{ display: { xs: "none", md: "flex" } }}>
          <IconButton
            size="large"
            aria-label="show 17 new notifications"
            color="inherit"
          >
            <Badge badgeContent={17} color="error">
              {/* <NotificationsIcon /> */}
              <NotificationsNoneOutlinedIcon sx={{ fontSize: 30 }} />
            </Badge>
          </IconButton>
        </Box>
      </Toolbar>
    </Box>
  );
}
