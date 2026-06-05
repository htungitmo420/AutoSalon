import { useState } from 'react';
import { Box, Button, TextField, Typography, Paper, Alert, IconButton } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import useThemeStore from '../store/themeStore';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';
import api from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const { mode, toggleMode } = useThemeStore();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const res = await api.post('/auth/login', { email, password });
      login({
        email: res.data.email,
        fullName: res.data.fullName,
        roles: res.data.roles || [],
      }, res.data.accessToken);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    }
  };

  return (
    <Box sx={{ 
      position: 'relative',
      minHeight: '100vh',
      width: '100vw',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      overflow: 'hidden',
      py: 6,
      px: 2
    }}>
      {/* Background Car Image */}
      <Box
        sx={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundImage: 'url(https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=1920&q=80)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          zIndex: -3,
          '&::after': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            bgcolor: mode === 'light' ? 'rgba(245, 247, 250, 0.45)' : 'rgba(8, 8, 10, 0.65)',
            transition: 'background-color 0.5s ease',
          }
        }}
      />

      {/* iOS 26 Fluid animated background spheres */}
      <div className="fluid-bg">
        <div className="fluid-orb fluid-orb-1" />
        <div className="fluid-orb fluid-orb-2" />
        <div className="fluid-orb fluid-orb-3" />
      </div>

      {/* Floating Theme Toggle Button */}
      <IconButton 
        onClick={toggleMode} 
        color="inherit" 
        sx={{ 
          position: 'absolute', 
          top: 24, 
          right: 24, 
          bgcolor: mode === 'light' ? 'rgba(255,255,255,0.45)' : 'rgba(18,18,22,0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          border: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.5)' 
            : '1px solid rgba(255, 255, 255, 0.1)',
          zIndex: 10,
          p: 1.5,
          '&:hover': {
            bgcolor: mode === 'light' ? 'rgba(255,255,255,0.65)' : 'rgba(18,18,22,0.85)',
            transform: 'scale(1.05)',
          },
          transition: 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)'
        }}
      >
        {mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
      </IconButton>

      {/* Centered Frosted Glass Panel */}
      <Paper
        elevation={0}
        component="form"
        onSubmit={handleSubmit}
        noValidate
        sx={{
          p: { xs: 4, md: 6 },
          width: '100%',
          maxWidth: '440px',
          borderRadius: 6,
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          border: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.5)' 
            : '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: mode === 'light'
            ? '0 20px 50px rgba(31, 38, 135, 0.05), inset 0 1px 0 0 rgba(255, 255, 255, 0.5)'
            : '0 20px 50px rgba(0, 0, 0, 0.4), inset 0 1px 0 0 rgba(255, 255, 255, 0.06)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          position: 'relative',
          zIndex: 1,
          animation: 'slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards',
          '@keyframes slideUp': {
            '0%': {
              opacity: 0,
              transform: 'translateY(20px)',
            },
            '100%': {
              opacity: 1,
              transform: 'translateY(0)',
            }
          }
        }}
      >
        <Typography 
          variant="h4" 
          fontWeight="900" 
          gutterBottom 
          sx={{ 
            fontFamily: '"Space Grotesk", sans-serif', 
            letterSpacing: 4, 
            textAlign: 'center',
            textTransform: 'uppercase',
            color: 'text.primary',
            mb: 1
          }}
        >
          AUTOSALON
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 4, textAlign: 'center' }}>
          Sign in to access your premium account
        </Typography>

        {error && (
          <Alert 
            severity="error" 
            sx={{ 
              width: '100%', 
              mb: 3, 
              borderRadius: 3,
              bgcolor: mode === 'light' ? 'rgba(253, 237, 237, 0.6)' : 'rgba(95, 33, 32, 0.3)',
              border: '1px solid rgba(255,0,0,0.1)',
              color: 'error.main'
            }}
          >
            {error}
          </Alert>
        )}

        <Box sx={{ width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="Email Address"
            name="email"
            autoComplete="email"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            sx={{ mb: 1 }}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Password"
            type="password"
            id="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            sx={{ mb: 2 }}
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            size="large"
            sx={{ 
              mt: 3, 
              mb: 3, 
              py: 1.8, 
              borderRadius: 4, 
              fontSize: '1.05rem', 
              fontWeight: 'bold',
              boxShadow: mode === 'light' 
                ? '0 8px 24px rgba(212, 175, 55, 0.2)' 
                : '0 8px 24px rgba(212, 175, 55, 0.3)'
            }}
          >
            Sign In
          </Button>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, alignItems: 'center', width: '100%' }}>
            <Typography 
              variant="body2" 
              component={RouterLink} 
              to="#" 
              sx={{ 
                color: 'primary.main', 
                textDecoration: 'none',
                fontWeight: 600,
                '&:hover': { textDecoration: 'underline' } 
              }}
            >
              Forgot password?
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Don't have an account?{' '}
              <Box 
                component={RouterLink} 
                to="#" 
                sx={{ 
                  color: 'primary.main', 
                  textDecoration: 'none',
                  fontWeight: 600,
                  display: 'inline',
                  '&:hover': { textDecoration: 'underline' } 
                }}
              >
                Sign Up
              </Box>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}
