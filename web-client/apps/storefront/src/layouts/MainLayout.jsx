import { Box, AppBar, Toolbar, Typography, Button, IconButton, Container, useScrollTrigger } from '@mui/material';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import useThemeStore from '../store/themeStore';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

function ElevationScroll(props) {
  const { children } = props;
  const trigger = useScrollTrigger({
    disableHysteresis: true,
    threshold: 0,
  });

  return children; // For now we just return children, could add elevation if needed
}

export default function MainLayout() {
  const { isAuthenticated, user, logout } = useAuthStore();
  const { mode, toggleMode } = useThemeStore();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isHome = location.pathname === '/';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'transparent' }}>
      {/* iOS 26 Fluid animated background spheres */}
      <div className="fluid-bg">
        <div className="fluid-orb fluid-orb-1" />
        <div className="fluid-orb fluid-orb-2" />
        <div className="fluid-orb fluid-orb-3" />
      </div>

      <ElevationScroll>
        <AppBar 
          position="fixed" 
          elevation={0}
          sx={{ 
            bgcolor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(8, 8, 10, 0.65)',
            backdropFilter: 'blur(30px) saturate(210%)',
            WebkitBackdropFilter: 'blur(30px) saturate(210%)',
            borderBottom: '1px solid',
            borderColor: 'divider',
            color: 'text.primary',
            py: 1.5,
            width: '100vw',
            left: 0,
            top: 0
          }}
        >
          <Container maxWidth="xl">
            <Toolbar disableGutters>
              <DirectionsCarIcon sx={{ display: { xs: 'none', md: 'flex' }, mr: 1, color: 'primary.main', fontSize: 32 }} />
              <Typography
                variant="h5"
                noWrap
                component={Link}
                to="/"
                sx={{
                  mr: 4,
                  display: { xs: 'none', md: 'flex' },
                  fontFamily: 'monospace',
                  fontWeight: 800,
                  letterSpacing: '.2rem',
                  color: 'inherit',
                  textDecoration: 'none',
                }}
              >
                AUTOSALON
              </Typography>

              <Box sx={{ flexGrow: 1, display: 'flex', gap: 2 }}>
                <Button component={Link} to="/catalog" sx={{ color: 'text.primary', fontWeight: 600, fontSize: '1rem' }}>
                  Vehicles
                </Button>
                <Button component={Link} to="/cart" sx={{ color: 'text.primary', fontWeight: 600, fontSize: '1rem' }}>
                  Cart
                </Button>
              </Box>

              <Box sx={{ flexGrow: 0, display: 'flex', alignItems: 'center', gap: 1 }}>
                <IconButton onClick={toggleMode} color="inherit" sx={{ mr: 1 }}>
                  {mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
                </IconButton>

                {isAuthenticated ? (
                  <>
                    <Button 
                      component={Link} 
                      to="/profile" 
                      startIcon={<AccountCircleIcon />}
                      sx={{ color: 'text.primary', fontWeight: 600, textTransform: 'none' }}
                    >
                      {user?.email}
                    </Button>
                    <Button variant="outlined" color="primary" onClick={handleLogout} sx={{ borderRadius: 20, ml: 1 }}>
                      Logout
                    </Button>
                  </>
                ) : (
                  <Button variant="contained" color="primary" component={Link} to="/login" sx={{ borderRadius: 20, px: 3, py: 1 }}>
                    Sign In
                  </Button>
                )}
              </Box>
            </Toolbar>
          </Container>
        </AppBar>
      </ElevationScroll>

      <Box component="main" sx={{ flexGrow: 1, pt: isHome ? 0 : '88px' }}>
        <Outlet />
      </Box>

      <Box component="footer" sx={{ 
        py: 6, 
        px: 2, 
        mt: 'auto',
        backgroundColor: mode === 'light' ? 'rgba(240, 240, 245, 0.4)' : 'rgba(10, 10, 12, 0.4)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderTop: '1px solid',
        borderColor: 'divider'
      }}>
        <Container maxWidth="lg">
          <Box display="flex" justifyContent="space-between" alignItems="center" flexWrap="wrap">
            <Typography variant="h6" fontWeight="bold" color="primary" sx={{ letterSpacing: 2 }}>
              AUTOSALON
            </Typography>
            <Typography variant="body2" color="text.secondary">
              © {new Date().getFullYear()} Auto Salon. Redefining the driving experience.
            </Typography>
          </Box>
        </Container>
      </Box>
    </Box>
  );
}
