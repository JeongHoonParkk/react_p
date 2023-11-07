import * as React from "react";
import Avatar from "@mui/material/Avatar";
import Stack from "@mui/material/Stack";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";

export default function Profile() {
  return (
    <Box sx={{ display: { xs: "none", md: "flex", marginLeft: "-30px" } }}>
      <Stack direction="row" spacing={2}>
        <IconButton>
          <Avatar alt="Remy Sharp" src="/static/images/avatar/1.jpg" />
        </IconButton>
      </Stack>
    </Box>
  );
}
