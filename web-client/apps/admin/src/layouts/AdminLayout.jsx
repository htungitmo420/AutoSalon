import { Box, Drawer, AppBar, Toolbar, Typography, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider, IconButton, Button } from '@mui/material';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import useThemeStore from '../store/themeStore';
import DashboardIcon from '@mui/icons-material/Dashboard';
import InventoryIcon from '@mui/icons-material/Inventory';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import BuildIcon from '@mui/icons-material/Build';
import LogoutIcon from '@mui/icons-material/Logout';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';

const drawerWidth = 260;

export default function AdminLayout() {
  const { user, logout } = useAuthStore();
  const { mode, toggleMode } = useThemeStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const roles = user?.roles?.length ? user.roles : [user?.role].filter(Boolean);
  const roleLabel = roles.join(', ');

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/', roles: ['MANAGER', 'ADMIN'] },
    { text: 'Orders & Drives', icon: <ShoppingCartIcon />, path: '/orders', roles: ['MANAGER', 'ADMIN'] },
    { text: 'Inventory', icon: <InventoryIcon />, path: '/inventory', roles: ['WAREHOUSE_ADMIN', 'ADMIN'] },
    { text: 'Assembly', icon: <BuildIcon />, path: '/assembly', roles: ['WAREHOUSE_ADMIN', 'ADMIN'] },
  ];

  const filteredMenu = menuItems.filter((item) => item.roles.some((role) => roles.includes(role)));

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'transparent' }}>
      {/* iOS 26 Fluid animated background spheres */}
      <div className="fluid-bg">
        <div className="fluid-orb fluid-orb-1" />
        <div className="fluid-orb fluid-orb-2" />
        <div className="fluid-orb fluid-orb-3" />
      </div>

      <AppBar 
        position="fixed" 
        sx={{ 
          width: `calc(100% - ${drawerWidth}px)`, 
          ml: `${drawerWidth}px`, 
          bgcolor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(8, 8, 10, 0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          borderBottom: '1px solid',
          borderColor: 'divider',
          color: 'text.primary',
          boxShadow: 'none'
        }}
      >
        <Toolbar>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            Admin Portal
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton onClick={toggleMode} color="inherit">
              {mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
            </IconButton>
            <Typography variant="body2" sx={{ opacity: 0.8 }}>
              Logged in as {user?.email} ({roleLabel})
            </Typography>
            <Button color="error" startIcon={<LogoutIcon />} onClick={handleLogout} sx={{ borderRadius: 20 }}>
              Logout
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      <Drawer
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(15, 23, 42, 0.6)',
            backdropFilter: 'blur(30px) saturate(180%)',
            WebkitBackdropFilter: 'blur(30px) saturate(180%)',
            borderRight: '1px solid',
            borderColor: 'divider',
            color: 'text.primary',
          },
        }}
        variant="permanent"
        anchor="left"
      >
        <Toolbar sx={{ justifyContent: 'center' }}>
          <Typography variant="h6" fontWeight="900" sx={{ letterSpacing: 2, color: 'primary.main' }}>
            AUTOSALON
          </Typography>
        </Toolbar>
        <Divider />
        <List sx={{ px: 1, py: 2 }}>
          {filteredMenu.map((item) => {
            const isSelected = location.pathname === item.path;
            return (
              <ListItem key={item.text} disablePadding>
                <ListItemButton 
                  component={Link} 
                  to={item.path}
                  selected={isSelected}
                  sx={{
                    borderRadius: 3,
                    mb: 0.5,
                    transition: 'all 0.2s ease',
                    '&.Mui-selected': { 
                      bgcolor: mode === 'light' ? 'rgba(25, 118, 210, 0.12)' : 'rgba(25, 118, 210, 0.25)',
                      color: 'primary.main',
                      fontWeight: 'bold',
                      '& .MuiListItemIcon-root': { color: 'primary.main' }
                    },
                    '&:hover': { 
                      bgcolor: mode === 'light' ? 'rgba(0, 0, 0, 0.04)' : 'rgba(255, 255, 255, 0.05)' 
                    }
                  }}
                >
                  <ListItemIcon sx={{ color: isSelected ? 'primary.main' : 'text.secondary', minWidth: 40 }}>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText 
                    primary={item.text} 
                    primaryTypographyProps={{ 
                      fontWeight: isSelected ? 700 : 500,
                      fontSize: '0.95rem'
                    }} 
                  />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, bgcolor: 'transparent', p: 4, minHeight: '100vh' }}>
        <Toolbar /> {/* Spacer for AppBar */}
        <Outlet />
      </Box>
    </Box>
  );
}
