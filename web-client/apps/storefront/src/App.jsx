import { Routes, Route } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Login from './pages/Login';
import Catalog from './pages/Catalog';
import Cart from './pages/Cart';
import { Box, Typography, Container, Button, Grid, Paper } from '@mui/material';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <Box>
      {/* Hero Section */}
      <Box 
        sx={{ 
          position: 'relative',
          height: { xs: 'auto', md: '100vh' },
          width: '100vw',
          marginLeft: 'calc(-50vw + 50%)', /* Ensure it bleeds to the edge if inside a container */
          display: 'flex',
          flexDirection: { xs: 'column', md: 'row' },
          overflow: 'hidden',
          bgcolor: 'transparent'
        }}
      >
        {/* Mobile Background Image (Only visible on xs/sm viewports) */}
        <Box
          sx={{
            display: { xs: 'block', md: 'none' },
            width: '100%',
            height: '45vh',
            backgroundImage: 'url("https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?auto=format&fit=crop&w=1000&q=80")',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />

        {/* Text/Button Container (Vertical glass panel on desktop, slide-up card below image on mobile) */}
        <Box
          sx={{
            width: { xs: '100%', md: '520px' },
            height: { xs: 'auto', md: '100%' },
            bgcolor: (theme) => theme.palette.mode === 'light' ? 'rgba(255, 255, 255, 0.4)' : 'rgba(10, 10, 12, 0.6)',
            backdropFilter: 'blur(40px) saturate(180%)',
            WebkitBackdropFilter: 'blur(40px) saturate(180%)',
            borderRight: (theme) => ({ xs: 'none', md: `1px solid ${theme.palette.divider}` }),
            borderBottom: (theme) => ({ xs: `1px solid ${theme.palette.divider}`, md: 'none' }),
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            px: { xs: 4, md: 6 },
            py: { xs: 6, md: 0 },
            pt: { xs: 4, md: 8 }, // Offset for top header
            zIndex: 2,
            boxShadow: { xs: 'none', md: '20px 0 80px rgba(0, 0, 0, 0.35)' },
            transition: 'background-color 0.5s ease',
            // Smooth entrance animation
            animation: 'slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards',
            '@keyframes slideUp': {
              '0%': {
                opacity: 0,
                transform: 'translateY(24px)',
              },
              '100%': {
                opacity: 1,
                transform: 'translateY(0)',
              }
            }
          }}
        >
          <Typography 
            variant="h2" 
            fontWeight="900" 
            gutterBottom 
            sx={{ 
              textTransform: 'uppercase', 
              letterSpacing: 2, 
              textShadow: (theme) => theme.palette.mode === 'light' ? 'none' : '2px 2px 4px rgba(0,0,0,0.5)', 
              color: 'text.primary',
              fontSize: { xs: '2.2rem', md: '3.5rem' }
            }}
          >
            Experience <Box component="span" color="primary.main">Perfection</Box>
          </Typography>
          <Typography 
            variant="h5" 
            paragraph 
            sx={{ 
              mb: 6, 
              fontWeight: 400, 
              color: 'text.secondary',
              fontSize: { xs: '1.05rem', md: '1.2rem' },
              lineHeight: 1.6
            }}
          >
            Discover our curated collection of premium vehicles. Engineered for performance, designed for elegance.
          </Typography>
          <Box gap={2} display="flex" flexDirection={{ xs: 'column', sm: 'row' }}>
            <Button component={Link} to="/catalog" variant="contained" color="primary" size="large" sx={{ px: 4, py: 1.8, fontSize: '1.1rem', flex: 1 }}>
              Explore Models
            </Button>
            <Button 
              component={Link} 
              to="/catalog" 
              variant="outlined" 
              sx={{ 
                color: 'text.primary', 
                borderColor: 'text.primary', 
                '&:hover': { 
                  borderColor: 'primary.main', 
                  bgcolor: 'rgba(212, 175, 55, 0.1)' 
                }, 
                px: 4, 
                py: 1.8, 
                fontSize: '1.1rem', 
                flex: 1 
              }}
            >
              Book Test Drive
            </Button>
          </Box>
        </Box>

        {/* Desktop Background Image (Only visible on md/lg viewports) */}
        <Box
          sx={{
            display: { xs: 'none', md: 'block' },
            flexGrow: 1,
            height: '100%',
            backgroundImage: 'linear-gradient(to right, rgba(0, 0, 0, 0.4) 0%, rgba(0, 0, 0, 0.1) 50%, rgba(0, 0, 0, 0) 100%), url("https://images.unsplash.com/photo-1603584173870-7f23fdae1b7a?auto=format&fit=crop&w=1920&q=80")',
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
      </Box>

      {/* Features Section */}
      <Box sx={{ py: 12, px: { xs: 3, md: 8, lg: 12 }, bgcolor: 'transparent' }}>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', mb: 8 }}>
          <Typography variant="h3" fontWeight="bold" gutterBottom sx={{ fontSize: { xs: '2rem', md: '2.75rem' } }}>
            Why Choose Auto Salon
          </Typography>
          <Typography variant="subtitle1" color="text.secondary" sx={{ maxWidth: '600px', mx: 'auto', lineHeight: 1.6 }}>
            Unmatched quality, transparent pricing, and exceptional service.
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4, justifyContent: 'center', mt: 4 }}>
          {[
            { title: 'Premium Selection', desc: 'Every vehicle passes a rigorous 150-point inspection.' },
            { title: 'Transparent Pricing', desc: 'No hidden fees. The price you see is the price you pay.' },
            { title: 'Home Delivery', desc: 'Complete your purchase online and we will deliver it to your door.' }
          ].map((feature, idx) => (
            <Box key={idx} sx={{ flex: 1 }}>
              <Paper 
                elevation={0} 
                sx={{ 
                  p: 4, 
                  height: '100%', 
                  textAlign: 'center', 
                  transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s ease', 
                  '&:hover': { 
                    transform: 'translateY(-10px)', 
                    boxShadow: (theme) => theme.palette.mode === 'light'
                      ? '0 20px 40px rgba(0, 0, 0, 0.08)'
                      : '0 20px 40px rgba(0, 0, 0, 0.5)'
                  } 
                }}
              >
                <Typography variant="h5" fontWeight="bold" gutterBottom color="primary.main">{feature.title}</Typography>
                <Typography color="text.secondary">{feature.desc}</Typography>
              </Paper>
            </Box>
          ))}
        </Box>
      </Box>
    </Box>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      <Route element={<MainLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/catalog" element={<Catalog />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/profile" element={<Typography variant="h4" sx={{p:4}}>Profile Page Coming Soon</Typography>} />
      </Route>
    </Routes>
  );
}

export default App;
