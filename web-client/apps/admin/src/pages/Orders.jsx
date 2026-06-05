import { useState } from 'react';
import { Container, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Box, Select, MenuItem } from '@mui/material';

export default function Orders() {
  const [orders, setOrders] = useState([
    { id: 'ORD-1001', customer: 'john@example.com', vehicle: 'Sedan X', total: 49500, status: 'PAID', date: '2026-06-03' },
    { id: 'ORD-1002', customer: 'alice@example.com', vehicle: 'SUV Y', total: 69500, status: 'WAITING_FOR_PAYMENT', date: '2026-06-04' },
    { id: 'ORD-1003', customer: 'bob@example.com', vehicle: 'Sports Z', total: 89500, status: 'READY_FOR_PICKUP', date: '2026-06-01' },
  ]);

  const handleStatusChange = (id, newStatus) => {
    setOrders(orders.map(o => o.id === id ? { ...o, status: newStatus } : o));
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PAID': return 'info';
      case 'WAITING_FOR_PAYMENT': return 'warning';
      case 'READY_FOR_PICKUP': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ px: { xs: 2, md: 4 }, py: 4, width: '100%' }}>
      <Typography variant="h4" fontWeight="bold" mb={4}>
        Orders Management
      </Typography>

      <TableContainer component={Paper} sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Table>
          <TableHead sx={{ bgcolor: 'background.default' }}>
            <TableRow>
              <TableCell>Order ID</TableCell>
              <TableCell>Customer</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.customer}</TableCell>
                <TableCell>{row.date}</TableCell>
                <TableCell>${row.total.toLocaleString()}</TableCell>
                <TableCell>
                  <Chip 
                    label={row.status} 
                    color={getStatusColor(row.status)} 
                    size="small" 
                    sx={{ fontWeight: 'bold' }}
                  />
                </TableCell>
                <TableCell align="right">
                  <Select
                    value={row.status}
                    size="small"
                    onChange={(e) => handleStatusChange(row.id, e.target.value)}
                    sx={{ width: 200 }}
                  >
                    <MenuItem value="WAITING_FOR_PAYMENT">WAITING_FOR_PAYMENT</MenuItem>
                    <MenuItem value="PAID">PAID</MenuItem>
                    <MenuItem value="READY_FOR_PICKUP">READY_FOR_PICKUP</MenuItem>
                    <MenuItem value="COMPLETED">COMPLETED</MenuItem>
                    <MenuItem value="CANCELLED">CANCELLED</MenuItem>
                  </Select>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
