import { Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from './layouts/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import Orders from './pages/Orders';
import { Typography, Paper, Box } from '@mui/material';
import useAuthStore from './store/authStore';

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      
      <Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="orders" element={<Orders />} />
        <Route path="inventory" element={<Inventory />} />
        <Route path="assembly" element={
          <Box p={3}>
            <Typography variant="h4" gutterBottom>Assembly Orders</Typography>
            <Paper sx={{ p: 4, borderRadius: 2 }}>Assembly Orders List.</Paper>
          </Box>
        } />
      </Route>
    </Routes>
  );
}

export default App;
