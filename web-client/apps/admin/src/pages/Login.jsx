import { useState } from 'react';
import { Box, Button, TextField, Typography, Paper, Alert, Container } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../store/authStore';
import api from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const res = await api.post('/auth/login', { email, password });
      const user = {
        email: res.data.email,
        fullName: res.data.fullName,
        roles: res.data.roles || [],
        role: res.data.roles?.[0],
      };
      
      if (!user.roles.some((role) => ['MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN'].includes(role))) {
        throw new Error('Access denied. Admin privileges required.');
      }
      
      login(user, res.data.accessToken);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Login failed.');
    }
  };

  return (
    <Box sx={{ width: '100vw', height: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'background.default' }}>
      <Paper elevation={4} sx={{ p: 4, width: '100%', maxWidth: 400, mx: 2, borderRadius: 2 }}>
        <Typography variant="h4" fontWeight="bold" gutterBottom textAlign="center">
          Auto Salon Admin
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Email"
            variant="outlined"
            margin="normal"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            type="email"
          />
          <TextField
            fullWidth
            label="Password"
            variant="outlined"
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            type="password"
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            size="large"
            sx={{ mt: 3, mb: 2 }}
          >
            Sign In
          </Button>
        </form>
      </Paper>
    </Box>
  );
}
